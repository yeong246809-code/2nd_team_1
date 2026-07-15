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
    private final OrderDeliveryRepository orderDeliveryRepository;
    private final ShopRepository shopRepository;
    private final CouponIssuanceService couponIssuanceService;

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
        List<NormalizedShipment> shipments = normalizeAndValidateShipments(request, lines);

        int totalProductPrice = lines.stream().mapToInt(Line::productTotal).sum();
        int productDiscountPrice = lines.stream().mapToInt(Line::discountTotal).sum();
        int originalShippingFee = groupedShippingFee(lines);
        Map<Long, Integer> productPrices = new LinkedHashMap<>();
        lines.forEach(line -> productPrices.merge(line.product().getProdNo(),
                line.productTotal() - line.discountTotal(), Integer::sum));
        CouponIssuanceService.AppliedCoupon appliedCoupon = request.getCouponDetailNo() == null
                ? null : couponIssuanceService.useForOrder(
                        request.getCouponDetailNo(), memberNo, productPrices, originalShippingFee);
        int couponProductDiscount = appliedCoupon == null || appliedCoupon.freeShipping()
                ? 0 : appliedCoupon.discount();
        int totalDiscountPrice = productDiscountPrice + couponProductDiscount;
        int totalShippingFee = appliedCoupon != null && appliedCoupon.freeShipping() ? 0 : originalShippingFee;
        int rewardPoints = lines.stream().mapToInt(Line::rewardTotal).sum();
        int beforePoints = totalProductPrice - totalDiscountPrice + totalShippingFee;
        int usedPoints = request.getUsedPoints() == null ? 0 : request.getUsedPoints();
        validatePoints(member.getPoints(), usedPoints, beforePoints);
        int totalPaymentPrice = beforePoints - usedPoints;
        String paymentLabel = PAYMENT_LABELS.get(request.getPaymentMethod());
        String status = "DEPOSIT".equals(request.getPaymentMethod()) ? "입금대기" : "결제완료";
        NormalizedShipment primaryShipment = shipments.get(0);
        String ordererName = firstText(request.getOrdererName(), firstText(member.getName(), request.getRecipientName()));
        String ordererPhone = firstText(request.getOrdererPhone(), firstText(member.getPhone(), request.getRecipientPhone()));
        String ordererZipCode = firstText(request.getOrdererZipCode(), firstText(member.getZipCode(), request.getZipCode()));
        String ordererBaseAddress = firstText(request.getOrdererBaseAddress(), firstText(member.getBaseAddress(), request.getBaseAddress()));
        String ordererDetailAddress = firstText(request.getOrdererDetailAddress(), firstText(member.getDetailAddress(), request.getDetailAddress()));
        validateOrderer(ordererName, ordererPhone, ordererBaseAddress);

        Order order = orderRepository.save(Order.builder()
                .memberNo(memberNo)
                .orderName(ordererName)
                .paymentMethod(paymentLabel)
                .totalProductPrice(totalProductPrice)
                .totalDiscountPrice(totalDiscountPrice)
                .totalShippingFee(totalShippingFee)
                .usedPoints(usedPoints)
                .totalPaymentPrice(totalPaymentPrice)
                .createdAt(LocalDateTime.now())
                .status(status)
                .recipientName(primaryShipment.recipientName())
                .recipientPhone(primaryShipment.recipientPhone())
                .zipCode(primaryShipment.zipCode())
                .baseAddress(primaryShipment.baseAddress())
                .detailAddress(primaryShipment.detailAddress())
                .memo(primaryShipment.memo())
                .ordererName(ordererName)
                .ordererPhone(ordererPhone)
                .ordererZipCode(valueOrEmpty(ordererZipCode))
                .ordererBaseAddress(ordererBaseAddress)
                .ordererDetailAddress(valueOrEmpty(ordererDetailAddress))
                .build());

        Map<Integer, Integer> maxShippingByShop = maxShippingByShop(lines);
        Set<Integer> chargedShops = new HashSet<>();
        List<OrderDetails> details = new ArrayList<>();
        int remainingCouponDiscount = couponProductDiscount;
        for (Line line : lines) {
            int shopNo = line.product().getShopNo();
            int maxShippingFee = maxShippingByShop.getOrDefault(shopNo, 0);
            int chargedShippingFee = appliedCoupon != null && appliedCoupon.freeShipping() ? 0
                    : line.shippingFee() == maxShippingFee && chargedShops.add(shopNo)
                    ? maxShippingFee : 0;
            boolean couponEligibleLine = appliedCoupon != null && !appliedCoupon.freeShipping()
                    && (appliedCoupon.prodNo() == null || appliedCoupon.prodNo().equals(line.product().getProdNo()));
            int allocatedCouponDiscount = couponEligibleLine
                    ? Math.min(remainingCouponDiscount, line.productTotal() - line.discountTotal()) : 0;
            remainingCouponDiscount -= allocatedCouponDiscount;
            details.add(OrderDetails.builder()
                    .orderNo(order.getOrderNo())
                    .productNo(line.product().getProdNo())
                    .skuNo(line.sku() == null ? null : line.sku().getSkuNo())
                    .shopNo(shopNo)
                    .quantity(line.quantity())
                    .price(line.unitPrice())
                    .discountPrice(line.discountTotal() + allocatedCouponDiscount)
                    .shippingFee(chargedShippingFee)
                    .rewardPoints(line.rewardTotal())
                    .status(status)
                    .build());
        }
        List<OrderDetails> savedDetails = orderDetailsRepository.saveAll(details);
        Map<String, OrderDetails> detailByItemKey = new HashMap<>();
        for (int i = 0; i < lines.size(); i++) {
            detailByItemKey.put(lines.get(i).itemKey(), savedDetails.get(i));
        }
        saveDeliveries(order.getOrderNo(), shipments, detailByItemKey);

        lines.forEach(this::decreaseStockAndIncreaseSales);
        applyUsedPoints(member, order.getOrderNo(), usedPoints);
        if (!source.carts().isEmpty()) {
            cartRepository.deleteAll(source.carts());
        }
        return new CheckoutResult(order.getOrderNo(), ordererName, totalPaymentPrice, status, paymentLabel);
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
        String productSummary = buildProductSummary(items.stream().map(ReceiptItem::productName).toList());
        return new CheckoutReceipt(
                order.getOrderNo(), productSummary, order.getPaymentMethod(), order.getStatus(),
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
                    .map(cart -> new SourceItem("cart:" + cart.getCartNo(), cart.getProdNo(),
                            cart.getSkuNo(), cart.getQuantity()))
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
                items.add(new SourceItem("direct:" + i,
                        request.getDirectProdNo(), skuNo, request.getDirectQuantities().get(i)));
            }
            return new OrderSource(items, List.of());
        }

        if (request.getDirectQuantity() == null) {
            throw new IllegalArgumentException("주문 상품 정보가 없습니다.");
        }
        return new OrderSource(List.of(new SourceItem("direct:0",
                request.getDirectProdNo(), request.getDirectSkuNo(), request.getDirectQuantity())), List.of());
    }

    private Line loadAndValidateLine(SourceItem item) {
        Product product = productRepository.findByIdForUpdate(item.prodNo())
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다: " + item.prodNo()));
        if (product.getShopNo() == null) {
            throw new IllegalArgumentException("판매자 정보가 없는 상품은 주문할 수 없습니다.");
        }
        boolean activeShop = shopRepository.findByShopNo(product.getShopNo())
                .map(shop -> "ACTIVE".equals(shop.getStatus()))
                .orElse(false);
        if (!activeShop) {
            throw new IllegalArgumentException("운영 중인 상점의 상품만 주문할 수 있습니다.");
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
        return new Line(item.itemKey(), product, sku, item.quantity(), unitPrice,
                productTotal, discountTotal, shippingFee, rewardTotal);
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
        if (!PAYMENT_LABELS.containsKey(request.getPaymentMethod())) {
            throw new IllegalArgumentException("결제방법을 선택해주세요.");
        }
    }

    private void validateOrderer(String name, String phone, String baseAddress) {
        if (isBlank(name)) throw new IllegalArgumentException("주문자 이름을 입력해주세요.");
        if (isBlank(phone)) throw new IllegalArgumentException("주문자 연락처를 입력해주세요.");
        if (isBlank(baseAddress)) throw new IllegalArgumentException("주문자 주소를 입력해주세요.");
    }

    private List<NormalizedShipment> normalizeAndValidateShipments(
            CheckoutRequest request, List<Line> lines) {
        Map<String, Integer> orderedQuantities = new LinkedHashMap<>();
        lines.forEach(line -> orderedQuantities.put(line.itemKey(), line.quantity()));

        List<CheckoutRequest.Shipment> requested = request.getShipments();
        if (requested == null || requested.isEmpty()) {
            CheckoutRequest.Shipment legacy = new CheckoutRequest.Shipment();
            legacy.setRecipientName(request.getRecipientName());
            legacy.setRecipientPhone(request.getRecipientPhone());
            legacy.setZipCode(request.getZipCode());
            legacy.setBaseAddress(request.getBaseAddress());
            legacy.setDetailAddress(request.getDetailAddress());
            legacy.setMemo(request.getMemo());
            List<CheckoutRequest.ShipmentItem> legacyItems = new ArrayList<>();
            orderedQuantities.forEach((key, quantity) -> {
                CheckoutRequest.ShipmentItem item = new CheckoutRequest.ShipmentItem();
                item.setItemKey(key);
                item.setQuantity(quantity);
                legacyItems.add(item);
            });
            legacy.setItems(legacyItems);
            requested = List.of(legacy);
        }

        Map<String, Integer> assignedQuantities = new HashMap<>();
        List<NormalizedShipment> normalized = new ArrayList<>();
        for (int shipmentIndex = 0; shipmentIndex < requested.size(); shipmentIndex++) {
            CheckoutRequest.Shipment shipment = requested.get(shipmentIndex);
            if (shipment == null) continue;
            if (isBlank(shipment.getRecipientName())) {
                throw new IllegalArgumentException((shipmentIndex + 1) + "번 배송지의 수령자를 입력해주세요.");
            }
            if (isBlank(shipment.getRecipientPhone())) {
                throw new IllegalArgumentException((shipmentIndex + 1) + "번 배송지의 연락처를 입력해주세요.");
            }
            if (isBlank(shipment.getBaseAddress())) {
                throw new IllegalArgumentException((shipmentIndex + 1) + "번 배송지의 주소를 입력해주세요.");
            }

            Map<String, Integer> allocations = new LinkedHashMap<>();
            if (shipment.getItems() != null) {
                for (CheckoutRequest.ShipmentItem item : shipment.getItems()) {
                    if (item == null || isBlank(item.getItemKey())) continue;
                    if (!orderedQuantities.containsKey(item.getItemKey())) {
                        throw new IllegalArgumentException("배송 상품 정보가 올바르지 않습니다.");
                    }
                    int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
                    if (quantity < 0) throw new IllegalArgumentException("배송 수량은 0 이상이어야 합니다.");
                    if (quantity > 0) allocations.merge(item.getItemKey(), quantity, Integer::sum);
                }
            }
            if (allocations.isEmpty()) {
                throw new IllegalArgumentException((shipmentIndex + 1) + "번 배송지에 보낼 상품 수량을 입력해주세요.");
            }
            allocations.forEach((key, quantity) -> assignedQuantities.merge(key, quantity, Integer::sum));
            normalized.add(new NormalizedShipment(
                    shipment.getRecipientName().trim(), shipment.getRecipientPhone().trim(),
                    valueOrEmpty(shipment.getZipCode()), shipment.getBaseAddress().trim(),
                    valueOrEmpty(shipment.getDetailAddress()), valueOrEmpty(shipment.getMemo()), allocations));
        }

        if (normalized.isEmpty()) throw new IllegalArgumentException("배송지를 하나 이상 입력해주세요.");
        orderedQuantities.forEach((key, orderedQuantity) -> {
            int assigned = assignedQuantities.getOrDefault(key, 0);
            if (assigned != orderedQuantity) {
                throw new IllegalArgumentException("상품별 배송 수량의 합계가 주문 수량과 일치해야 합니다.");
            }
        });
        return normalized;
    }

    private void saveDeliveries(long orderNo, List<NormalizedShipment> shipments,
                                Map<String, OrderDetails> detailByItemKey) {
        List<OrderDelivery> deliveries = new ArrayList<>();
        for (int shipmentIndex = 0; shipmentIndex < shipments.size(); shipmentIndex++) {
            NormalizedShipment shipment = shipments.get(shipmentIndex);
            for (Map.Entry<String, Integer> allocation : shipment.allocations().entrySet()) {
                OrderDetails detail = detailByItemKey.get(allocation.getKey());
                if (detail == null) throw new IllegalArgumentException("배송 상품을 찾을 수 없습니다.");
                deliveries.add(OrderDelivery.builder()
                        .orderNo(orderNo)
                        .orderDetailNo(detail.getOrderDetailNo())
                        .shipmentIndex(shipmentIndex)
                        .quantity(allocation.getValue())
                        .recipientName(shipment.recipientName())
                        .recipientPhone(shipment.recipientPhone())
                        .zipCode(shipment.zipCode())
                        .baseAddress(shipment.baseAddress())
                        .detailAddress(shipment.detailAddress())
                        .memo(shipment.memo())
                        .build());
            }
        }
        orderDeliveryRepository.saveAll(deliveries);
    }

    private int groupedShippingFee(List<Line> lines) {
        return maxShippingByShop(lines).values().stream().mapToInt(Integer::intValue).sum();
    }

    private Map<Integer, Integer> maxShippingByShop(List<Line> lines) {
        Map<Integer, Integer> result = new HashMap<>();
        lines.forEach(line -> result.merge(line.product().getShopNo(), line.shippingFee(), Math::max));
        return result;
    }

    private void validatePoints(int currentPoints, int usedPoints, int paymentPrice) {
        if (usedPoints < 0) throw new IllegalArgumentException("사용 포인트는 0 이상이어야 합니다.");
        if (usedPoints > 0 && currentPoints < 5_000) {
            throw new IllegalArgumentException("포인트는 5,000점 이상 보유한 경우 사용할 수 있습니다.");
        }
        if (usedPoints > currentPoints) throw new IllegalArgumentException("보유 포인트를 초과할 수 없습니다.");
        if (usedPoints > paymentPrice) throw new IllegalArgumentException("결제금액보다 많은 포인트를 사용할 수 없습니다.");
    }

    private String buildProductSummary(List<String> productNames) {
        if (productNames.isEmpty()) return "상품 정보 없음";
        String name = productNames.get(0);
        if (productNames.size() > 1) name += " 외 " + (productNames.size() - 1) + "건";
        return name.length() <= 20 ? name : name.substring(0, 20);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String firstText(String preferred, String fallback) {
        return isBlank(preferred) ? valueOrEmpty(fallback) : preferred.trim();
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

    private record SourceItem(String itemKey, long prodNo, Long skuNo, int quantity) {}
    private record OrderSource(List<SourceItem> items, List<Cart> carts) {}
    private record Line(String itemKey, Product product, ProductSku sku, int quantity, int unitPrice,
                        int productTotal, int discountTotal, int shippingFee, int rewardTotal) {}
    private record NormalizedShipment(String recipientName, String recipientPhone, String zipCode,
                                      String baseAddress, String detailAddress, String memo,
                                      Map<String, Integer> allocations) {}
}
