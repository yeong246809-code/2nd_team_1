package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.DashboardStatsDTO;
import org.example.k_market.dto.DeliveryDTO;
import org.example.k_market.entity.Deliveries;
import org.example.k_market.entity.Order;
import org.example.k_market.entity.OrderDetails;
import org.example.k_market.entity.Product;
import org.example.k_market.repository.DeliveryRepository;
import org.example.k_market.repository.OrderDetailsRepository;
import org.example.k_market.repository.OrderRepository;
import org.example.k_market.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void registerDelivery(DeliveryDTO deliveryDTO) {
        // 1. 배송 정보 저장
        deliveryRepository.save(deliveryDTO.toEntity());

        orderDetailsRepository.updateStatus(deliveryDTO.getOrderDetailNo(), "배송중");

    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryDTO> getAllDeliveries() {
        // 1. 모든 배송 정보를 가져옴
        List<Deliveries> deliveryEntities = deliveryRepository.findAll();

        // 2. 각 배송 정보에 해당하는 주문 정보를 합쳐서 DTO로 변환
        return deliveryEntities.stream().map(d -> {
            DeliveryDTO dto = d.toDTO();
            // orderRepository를 이용해 주문 정보 조회 후 DTO에 채워넣기
            Order order = orderRepository.findById((int) d.getOrderNo()).orElse(null);
            if (order != null) {
                dto.setRecipientName(order.getRecipientName()); // 수령인 정보 주입
                // 필요 시 총 금액, 상품명 등도 여기서 세팅 가능
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<DeliveryDTO> getDeliveryList() {
        return null;
    }


    @Override
    @Transactional(readOnly = true)
    public Page<DeliveryDTO> getDeliveryList(Pageable pageable) {
        return mapDeliveryPage(deliveryRepository.findAll(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeliveryDTO> getSellerDeliveryList(long shopNo, Pageable pageable) {
        return mapDeliveryPage(deliveryRepository.findByShopNo(shopNo, pageable));
    }

    private Page<DeliveryDTO> mapDeliveryPage(Page<Deliveries> deliveries) {
        return deliveries.map(d -> {
            Order o = orderRepository.findById((int) d.getOrderNo()).orElse(new Order());

            String status = "배송준비"; // 기본값
            if (d.getShippedAt() != null) {
                long daysPassed = ChronoUnit.DAYS.between(d.getShippedAt().toLocalDate(), LocalDate.now());

                if (daysPassed >= 3) {
                    status = "배송완료";
                } else if (daysPassed >= 1) {
                    status = "배송중";
                } else {
                    status = "배송준비";
                }
            }

            // 상세 주문 조회 후 수량 합산 (0으로 초기화하여 Null 방지)
            int totalQuantity = orderDetailsRepository.findByOrderNo((int) d.getOrderNo())
                    .stream()
                    .mapToInt(detail -> detail.getQuantity())
                    .sum();

            return DeliveryDTO.builder()
                    .trackingNumber(d.getTrackingNumber() != null ? d.getTrackingNumber() : "미등록")
                    .courierName(d.getCourierName() != null ? d.getCourierName() : "미지정")
                    .orderNo(d.getOrderNo())
                    .recipientName(o.getRecipientName() != null ? o.getRecipientName() : "비회원")
                    .orderName(o.getOrderName() != null ? o.getOrderName() : "상품명없음")
                    .totalProductPrice(o.getTotalProductPrice())
                    .shippingFee(o.getTotalShippingFee())
                    .deliveryStatus(o.getStatus() != null ? o.getStatus() : "배송준비")
                    .shippedAt(d.getShippedAt())
                    .quantity(totalQuantity) // 이제 문제없음
                    .build();
        });
    }

    @Override
    public DeliveryDTO getDetailByTrackingNumber(String trackingNumber) {
        Deliveries d = deliveryRepository.findByTrackingNumber(trackingNumber);
        Order o = orderRepository.findById((int) d.getOrderNo()).orElseThrow();

        // 수정: 클래스명이 아니라 주입된 인스턴스명을 사용하세요!
        OrderDetails od = orderDetailsRepository.findByOrderNo((int) o.getOrderNo())
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("주문 상세 정보를 찾을 수 없습니다."));

        Product p = productRepository.findById(od.getProductNo()).orElseThrow();

        return DeliveryDTO.builder()
                .orderDetailNo(d.getOrderDetailNo())
                .trackingNumber(d.getTrackingNumber())
                .orderNo(d.getOrderNo())
                .recipientName(o.getRecipientName())
                .recipientPhone(o.getRecipientPhone())       // 실제 DB 데이터 사용
                .baseAddress(o.getBaseAddress())   // 실제 DB 데이터 사용
                .memo(o.getMemo())
                .courierName(d.getCourierName())
                .orderName(p.getName()) // 상품명
                .productImage(p.getThumb1())   // 상품 썸네일
                .totalProductPrice(p.getPrice())    // 상품 가격
                .quantity(od.getQuantity())       // 주문 수량
                .shippingFee(o.getTotalShippingFee())
                .totalProductPrice(o.getTotalProductPrice())
                .build();
    }

    @Override
    public DashboardStatsDTO getTodayDashboardStats() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);

        // 1. 오늘 주문건수 & 주문금액 조회
        List<Object[]> metrics = orderRepository.getOrderMetricsByDateRange(startOfToday, endOfToday);
        long orderCount = 0L;
        long orderAmount = 0L;
        if (!metrics.isEmpty() && metrics.get(0) != null) {
            orderCount = ((Number) metrics.get(0)[0]).longValue();
            orderAmount = ((Number) metrics.get(0)[1]).longValue();
        }

        // 2. 운영현황 상태별 카운트 (DB에 저장되는 실제 status 단어로 매핑)
        long pending = orderRepository.countByStatusLike("대기");
        long preparing = orderRepository.countByStatusLike("결제완료") + orderRepository.countByStatusLike("배송준비");
        long cancel = orderRepository.countByStatusLike("취소");
        long exchange = orderRepository.countByStatusLike("교환");
        long returnReq = orderRepository.countByStatusLike("반품");

        return DashboardStatsDTO.builder()
                .pendingDeposit(pending)
                .preparingDelivery(preparing)
                .cancelRequest(cancel)
                .exchangeRequest(exchange)
                .returnRequest(returnReq)
                .orderCount(orderCount)
                .orderAmount(orderAmount)
                .memberJoinCount(3L) // ※ 회원가입 카운트 쿼리가 있다면 대체: usersRepository.countByCreatedAtBetween(startOfToday, endOfToday)
                .visitorCount(128L)  // ※ 방문자 로그 테이블이 없다면 임시 통계값 적용
                .qnaCount(2L)        // ※ QnA 카운트 쿼리가 있다면 대체: qnaRepository.countByCreatedAtBetween(...)
                .build();
    }

    // ==========================================
    // [대시보드] 어제 기준 주요지표
    // ==========================================
    @Override
    public DashboardStatsDTO getYesterdayDashboardStats() {
        LocalDateTime startOfYesterday = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime endOfYesterday = startOfYesterday.plusDays(1);

        List<Object[]> metrics = orderRepository.getOrderMetricsByDateRange(startOfYesterday, endOfYesterday);
        long orderCount = 0L;
        long orderAmount = 0L;
        if (!metrics.isEmpty() && metrics.get(0) != null) {
            orderCount = ((Number) metrics.get(0)[0]).longValue();
            orderAmount = ((Number) metrics.get(0)[1]).longValue();
        }

        return DashboardStatsDTO.builder()
                .orderCount(orderCount)
                .orderAmount(orderAmount)
                .memberJoinCount(5L) // 어제 가입자수 예시
                .visitorCount(110L)  // 어제 방문자수 예시
                .qnaCount(4L)        // 어제 문의수 예시
                .build();
    }
}
