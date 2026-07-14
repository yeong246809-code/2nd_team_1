package org.example.k_market.service;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.CheckoutRequest;
import org.example.k_market.entity.*;
import org.example.k_market.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CheckoutService {
    private static final Map<String, String> PAYMENT_LABELS = Map.of(
            "CARD", "신용카드",
            "BANK_TRANSFER", "계좌이체",
            "MOBILE", "휴대폰결제",
            "CHECK_CARD", "체크카드",
            "DEPOSIT", "무통장입금",
            "KAKAO_PAY", "카카오페이"
    );

    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;
    private final MemberRepository memberRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    public CheckoutResult placeOrder(int memberNo, CheckoutRequest request) {
        validateRequest(request);
        Member member = memberRepository.findByIdForUpdate(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        OrderSource source = resolveSource(memberNo, request);
        List<Line> lines = new ArrayList<>();
        for (SourceItem item : source.items().stream().sorted(Comparator.comparing(SourceItem::prodNo)).toList()) {
            lines.add(loadAndValidateLine(item));
        }

        int totalProductPrice = lines.stream().mapToInt(Line::productTotal).sum();
        int totalDiscountPrice = lines.stream().mapToInt(Line::discountTotal).sum();
        int totalShippingFee = lines.stream().mapToInt(Line::shippingFee).sum();
        int rewardPoints = lines.stream().mapToInt(Line::rewardTotal).sum();
        int beforePoints = totalProductPrice - totalDiscountPrice + totalShippingFee;
        int usedPoints = request.getUsedPoints() == null ? 0 : request.getUsedPoints();
        validatePoints(member.getPoints(), usedPoints, beforePoints);
        int totalPaymentPrice = beforePoints - usedPoints;
        String paymentLabel = PAYMENT_LABELS.get(request.getPaymentMethod());
        String status = "DEPOSIT".equals(request.getPaymentMethod()) ? "입금대기" : "결제완료";
        String orderName = buildOrderName(lines);

        Order order = orderRepository.save(Order.builder()
                .memberNo(memberNo)
                .orderName(orderName)
                .paymentMethod(paymentLabel)
                .totalProductPrice(totalProductPrice)
                .totalDiscountPrice(totalDiscountPrice)
                .totalShippingFee(totalShippingFee)
                .usedPoints(usedPoints)
                .totalPaymentPrice(totalPaymentPrice)
                .createdAt(LocalDateTime.now())
                .status(status)
                .recipientName(request.getRecipientName().trim())
                .recipientPhone(request.getRecipientPhone().trim())
                .zipCode(valueOrEmpty(request.getZipCode()))
                .baseAddress(request.getBaseAddress().trim())
                .detailAddress(valueOrEmpty(request.getDetailAddress()))
                .memo(valueOrEmpty(request.getMemo()))
                .build());

        List<OrderDetails> details = lines.stream().map(line -> OrderDetails.builder()
                .orderNo(order.getOrderNo())
                .productNo(line.product().getProdNo())
                .skuNo(line.sku() == null ? null : line.sku().getSkuNo())
                .shopNo(line.product().getShopNo())
                .quantity(line.quantity())
                .price(line.unitPrice())
                .discountPrice(line.discountTotal())
                .shippingFee(line.shippingFee())
                .rewardPoints(line.rewardTotal())
                .status(status)
                .build()).toList();
        orderDetailsRepository.saveAll(details);

        lines.forEach(this::decreaseStockAndIncreaseSales);
        applyUsedPoints(member, order.getOrderNo(), usedPoints);
        if (!source.carts().isEmpty()) {
            cartRepository.deleteAll(source.carts());
        }
        return new CheckoutResult(order.getOrderNo(), orderName, totalPaymentPrice, status, paymentLabel);
    }

    @Transactional(readOnly = true)
    public CheckoutReceipt getReceipt(int memberNo, int orderNo) {
        Order order = orderRepository.findById(orderNo)
                .filter(found -> found.getMemberNo() == memberNo)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        List<ReceiptItem> items = orderDetailsRepository.findByOrderNo(orderNo).stream()
                .map(detail -> {
                    Product product = productRepository.findById(detail.getProductNo()).orElse(null);
                    ProductSku sku = detail.getSkuNo() == null ? null
                            : productSkuRepository.findById(detail.getSkuNo()).orElse(null);
                    int linePaymentPrice = detail.getPrice() * detail.getQuantity()
                            - detail.getDiscountPrice() + detail.getShippingFee();
                    return new ReceiptItem(
                            product == null ? "상품 정보 없음" : product.getName(),
                            sku == null ? null : sku.getSkuName(),
                            product == null ? null : product.getThumb1(),
                            detail.getQuantity(), detail.getPrice(), detail.getDiscountPrice(),
                            detail.getShippingFee(), linePaymentPrice, detail.getStatus());
                })
                .toList();
        return new CheckoutReceipt(
                order.getOrderNo(), order.getOrderName(), order.getPaymentMethod(), order.getStatus(),
                order.getCreatedAt(), order.getTotalProductPrice(), order.getTotalDiscountPrice(),
                order.getTotalShippingFee(), order.getUsedPoints(), order.getTotalPaymentPrice(),
                order.getRecipientName(), order.getRecipientPhone(), order.getZipCode(),
                order.getBaseAddress(), order.getDetailAddress(), order.getMemo(), items);
    }

    private OrderSource resolveSource(int memberNo, CheckoutRequest request) {
        if (request.isCartOrder()) {
            List<Long> cartNos = request.getCartNos().stream().distinct().toList();
            List<Cart> carts = cartRepository.findByMemberNoAndCartNoIn(memberNo, cartNos);
            if (carts.size() != cartNos.size()) {
                throw new IllegalArgumentException("본인의 장바구니 상품만 주문할 수 있습니다.");
            }
            List<SourceItem> items = carts.stream()
                    .map(cart -> new SourceItem(cart.getProdNo(), cart.getSkuNo(), cart.getQuantity()))
                    .toList();
            return new OrderSource(items, carts);
        }
        if (request.getDirectProdNo() == null) {
            throw new IllegalArgumentException("주문 상품 정보가 없습니다.");
        }

        if (request.getDirectSkuNos() != null && !request.getDirectSkuNos().isEmpty()) {
            if (request.getDirectQuantities() == null
                    || request.getDirectSkuNos().size() != request.getDirectQuantities().size()) {
                throw new IllegalArgumentException("바로구매 상품 정보가 올바르지 않습니다.");
            }
            List<SourceItem> items = new ArrayList<>();
            for (int i = 0; i < request.getDirectSkuNos().size(); i++) {
                String skuValue = request.getDirectSkuNos().get(i);
                Long skuNo = "none".equals(skuValue) ? null : Long.valueOf(skuValue);
                items.add(new SourceItem(
                        request.getDirectProdNo(), skuNo, request.getDirectQuantities().get(i)));
            }
            return new OrderSource(items, List.of());
        }

        if (request.getDirectQuantity() == null) {
            throw new IllegalArgumentException("주문 상품 정보가 없습니다.");
        }
        return new OrderSource(List.of(new SourceItem(
                request.getDirectProdNo(), request.getDirectSkuNo(), request.getDirectQuantity())), List.of());
    }

    private Line loadAndValidateLine(SourceItem item) {
        Product product = productRepository.findByIdForUpdate(item.prodNo())
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다: " + item.prodNo()));
        if (product.getShopNo() == null) {
            throw new IllegalArgumentException("판매자 정보가 없는 상품은 주문할 수 없습니다.");
        }
        ProductSku sku = null;
        int stock;
        if (item.skuNo() != null) {
            sku = productSkuRepository.findBySkuNoAndProdNoForUpdate(item.skuNo(), product.getProdNo())
                    .orElseThrow(() -> new IllegalArgumentException("선택한 상품 옵션이 올바르지 않습니다."));
            stock = sku.getStock() == null ? 0 : sku.getStock();
        } else {
            if (!productSkuRepository.findByProdNoOrderBySkuNoAsc(product.getProdNo()).isEmpty()) {
                throw new IllegalArgumentException("상품 옵션을 다시 선택해주세요.");
            }
            stock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        }
        if (item.quantity() < 1 || stock < item.quantity()) {
            throw new IllegalArgumentException(stock <= 0 ? "품절된 상품이 포함되어 있습니다."
                    : product.getName() + "의 재고는 최대 " + stock + "개입니다.");
        }

        int unitPrice = (product.getPrice() == null ? 0 : product.getPrice())
                + (sku == null || sku.getPrice() == null ? 0 : sku.getPrice());
        int discountRate = product.getDiscountRate() == null ? 0 : product.getDiscountRate();
        int discountedUnitPrice = (int) (unitPrice * (100L - discountRate) / 100L);
        int productTotal = unitPrice * item.quantity();
        int discountTotal = productTotal - discountedUnitPrice * item.quantity();
        int shippingFee = product.getShippingFee() == null ? 0 : product.getShippingFee();
        int rewardTotal = (product.getRewardPoints() == null ? 0 : product.getRewardPoints()) * item.quantity();
        return new Line(product, sku, item.quantity(), unitPrice, productTotal, discountTotal, shippingFee, rewardTotal);
    }

    private void decreaseStockAndIncreaseSales(Line line) {
        if (line.sku() != null) {
            line.sku().decreaseStock(line.quantity());
        } else {
            line.product().setStockQuantity(line.product().getStockQuantity() - line.quantity());
        }
        int salesCount = line.product().getSalesCount() == null ? 0 : line.product().getSalesCount();
        line.product().setSalesCount(salesCount + line.quantity());
    }

    private void applyUsedPoints(Member member, int orderNo, int usedPoints) {
        int remained = member.getPoints();
        if (usedPoints > 0) {
            remained -= usedPoints;
            pointHistoryRepository.save(PointHistory.builder()
                    .memberNo(member.getMemberNo())
                    .amount(-usedPoints)
                    .remainedAmount(remained)
                    .description("상품 구매 시 포인트 사용 (주문번호: " + orderNo + ")")
                    .createdAt(LocalDateTime.now())
                    .build());
        }
        member.changePoints(remained);
    }

    private void validateRequest(CheckoutRequest request) {
        if (request == null) throw new IllegalArgumentException("주문 정보가 없습니다.");
        if (isBlank(request.getRecipientName())) throw new IllegalArgumentException("수령자를 입력해주세요.");
        if (isBlank(request.getRecipientPhone())) throw new IllegalArgumentException("연락처를 입력해주세요.");
        if (isBlank(request.getBaseAddress())) throw new IllegalArgumentException("배송주소를 입력해주세요.");
        if (!PAYMENT_LABELS.containsKey(request.getPaymentMethod())) {
            throw new IllegalArgumentException("결제방법을 선택해주세요.");
        }
    }

    private void validatePoints(int currentPoints, int usedPoints, int paymentPrice) {
        if (usedPoints < 0) throw new IllegalArgumentException("사용 포인트는 0 이상이어야 합니다.");
        if (usedPoints > 0 && currentPoints < 5_000) {
            throw new IllegalArgumentException("포인트는 5,000점 이상 보유한 경우 사용할 수 있습니다.");
        }
        if (usedPoints > currentPoints) throw new IllegalArgumentException("보유 포인트를 초과할 수 없습니다.");
        if (usedPoints > paymentPrice) throw new IllegalArgumentException("결제금액보다 많은 포인트를 사용할 수 없습니다.");
    }

    private String buildOrderName(List<Line> lines) {
        String name = lines.get(0).product().getName();
        if (lines.size() > 1) name += " 외 " + (lines.size() - 1) + "건";
        return name.length() <= 20 ? name : name.substring(0, 20);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record CheckoutResult(int orderNo, String orderName, int totalPaymentPrice,
                                 String status, String paymentMethod) {}

    public record CheckoutReceipt(int orderNo, String orderName, String paymentMethod, String status,
                                  LocalDateTime createdAt, int totalProductPrice, int totalDiscountPrice,
                                  int totalShippingFee, int usedPoints, int totalPaymentPrice,
                                  String recipientName, String recipientPhone, String zipCode,
                                  String baseAddress, String detailAddress, String memo,
                                  List<ReceiptItem> items) {}

    public record ReceiptItem(String productName, String skuName, String thumb1, int quantity,
                              int unitPrice, int discountPrice, int shippingFee,
                              int linePaymentPrice, String status) {}

    private record SourceItem(long prodNo, Long skuNo, int quantity) {}
    private record OrderSource(List<SourceItem> items, List<Cart> carts) {}
    private record Line(Product product, ProductSku sku, int quantity, int unitPrice,
                        int productTotal, int discountTotal, int shippingFee, int rewardTotal) {}
}
