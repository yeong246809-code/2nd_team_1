package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.*;
import org.example.k_market.entity.Deliveries;
import org.example.k_market.entity.Order;
import org.example.k_market.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final DeliveryRepository deliveriesRepository;
    private final MemberRepository memberRepository; // <-- [추가] 실제 회원 레포지토리
    private final QnaRepository qnaRepository;       // <-- [추가] 실제 QnA 레포지토리
    private final ProductRepository productRepository;
    private final VisitorService visitorService;     // <-- [추가] 실제 방문자 서비스

    @Override
    public Page<OrderDTO> findAllOrders(Pageable pageable) {
        return orderRepository.findAllWithJoin(pageable).map(order -> {
            OrderDTO dto = order.toDTO();
            if (order.getUser() != null) {
                dto.setId(order.getUser().getId());
            }
            return dto;
        });
    }

    @Override
    public OrderDTO findOrderDetail(int orderNo) {
        Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        OrderDTO dto = order.toDTO();
        dto.setOrderItems(orderDetailsRepository.findByOrderNo(orderNo).stream().map(detail -> {
            OrderDetailsDTO item = detail.toDTO();
            productRepository.findById(detail.getProductNo())
                    .ifPresent(p -> item.setProdName(p.getName()));
            item.setTotalPaymentPrice((detail.getPrice() * detail.getQuantity()) - detail.getDiscountPrice());
            return item;
        }).toList());
        return dto;
    }

    @Override
    public OrderDTO findSellerOrderDetail(long shopNo, int orderNo) {
        Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        var details = orderDetailsRepository.findByOrderNoAndShopNo(orderNo, shopNo);
        if (details.isEmpty()) {
            throw new org.springframework.security.access.AccessDeniedException("다른 판매자의 주문입니다.");
        }
        OrderDTO dto = toSellerOrderDTO(order, details);
        dto.setOrderItems(details.stream().map(detail -> {
            OrderDetailsDTO item = detail.toDTO();
            productRepository.findById(detail.getProductNo()).ifPresent(p -> item.setProdName(p.getName()));
            item.setTotalPaymentPrice((detail.getPrice() * detail.getQuantity()) - detail.getDiscountPrice());
            return item;
        }).toList());
        return dto;
    }

    // 3. 주문 상태 변경
    @Transactional
    @Override
    public void updateOrderStatus(int orderNo, String status) {
        Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        order.setStatus(status);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void updateSellerOrderStatus(long shopNo, int orderNo, String status) {
        int updated = orderDetailsRepository.updateStatusByOrderNoAndShopNo(orderNo, shopNo, status);
        if (updated == 0) {
            throw new org.springframework.security.access.AccessDeniedException("다른 판매자의 주문입니다.");
        }
    }

    public Page<OrderDTO> findOrderList(Pageable pageable, String searchType, String keyword) {
        String normalizedType = "memberId".equals(searchType) ? "memberId" : "orderNo";
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        return orderRepository.findAdminOrders(normalizedType, normalizedKeyword, pageable)
                .map(Order::toDTO);
    }

    @Override
    public Page<OrderDTO> findSellerOrderList(long shopNo, Pageable pageable, String searchType, String keyword) {
        return orderRepository.findSellerOrders(shopNo, searchType, keyword, pageable)
                .map(order -> toSellerOrderDTO(
                        order, orderDetailsRepository.findByOrderNoAndShopNo(order.getOrderNo(), shopNo)));
    }

    private OrderDTO toSellerOrderDTO(Order order, java.util.List<org.example.k_market.entity.OrderDetails> details) {
        OrderDTO dto = order.toDTO();
        dto.setTotalProductPrice(details.stream().mapToInt(d -> d.getPrice() * d.getQuantity()).sum());
        dto.setTotalDiscountPrice(details.stream().mapToInt(org.example.k_market.entity.OrderDetails::getDiscountPrice).sum());
        dto.setTotalShippingFee(details.stream().mapToInt(org.example.k_market.entity.OrderDetails::getShippingFee).sum());
        dto.setTotalPaymentPrice(details.stream()
                .mapToInt(d -> d.getPrice() * d.getQuantity() - d.getDiscountPrice() + d.getShippingFee()).sum());
        if (!details.isEmpty()) dto.setStatus(details.get(0).getStatus());
        return dto;
    }

    @Override
    public boolean updateOrderStatus(String orderNo, String status) {
        return false;
    }

    @Override
    public OrderDTO getOrderDetail(int orderNo) {
        return findOrderDetail(orderNo);
    }

    @Transactional
    public void registerDelivery(DeliveryDTO dto) {
        Deliveries delivery = dto.toEntity();
        deliveriesRepository.save(delivery);
        Order order = orderRepository.findById((int)dto.getOrderNo()).orElseThrow();
        order.setStatus("결제완료(배송중)");
    }

    // ==========================================
    // [대시보드] 오늘 기준 운영현황 + 주요지표 (100% 실시간 DB 연동)
    // ==========================================
    @Override
    public DashboardStatsDTO getTodayDashboardStats() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);

        // 1. 주문 건수 & 금액
        List<Object[]> metrics = orderRepository.getOrderMetricsByDateRange(startOfToday, endOfToday);
        long orderCount = 0L;
        long orderAmount = 0L;
        if (!metrics.isEmpty() && metrics.get(0) != null) {
            orderCount = ((Number) metrics.get(0)[0]).longValue();
            orderAmount = ((Number) metrics.get(0)[1]).longValue();
        }

        // 2. 상태별 주문 현황
        long pending = orderRepository.countByStatusLike("대기");
        long preparing = orderRepository.countByStatusLike("결제완료") + orderRepository.countByStatusLike("배송준비");
        long cancel = orderRepository.countByStatusLike("취소");
        long exchange = orderRepository.countByStatusLike("교환");
        long returnReq = orderRepository.countByStatusLike("반품");

        // 3. 실제 DB 카운트 조회 (회원가입, 방문자수, 문의수)
        long memberJoinCount = memberRepository.countByCreatedAtBetween(startOfToday, endOfToday);
        long visitorCount = visitorService.getVisitorCount(LocalDate.now());
        long qnaCount = qnaRepository.countByCreatedAtBetween(startOfToday, endOfToday);

        return DashboardStatsDTO.builder()
                .pendingDeposit(pending)
                .preparingDelivery(preparing)
                .cancelRequest(cancel)
                .exchangeRequest(exchange)
                .returnRequest(returnReq)
                .orderCount(orderCount)
                .orderAmount(orderAmount)
                .memberJoinCount(memberJoinCount) // <-- 진짜 DB 가입자 수!
                .visitorCount(visitorCount)       // <-- 진짜 DB 방문자 수!
                .qnaCount(qnaCount)               // <-- 진짜 DB 문의글 수!
                .build();
    }

    // ==========================================
    // [대시보드] 어제 기준 주요지표 (100% 실시간 DB 연동)
    // ==========================================
    @Override
    public DashboardStatsDTO getYesterdayDashboardStats() {
        LocalDateTime startOfYesterday = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime endOfYesterday = startOfYesterday.plusDays(1);

        // 1. 어제 주문 건수 & 금액
        List<Object[]> metrics = orderRepository.getOrderMetricsByDateRange(startOfYesterday, endOfYesterday);
        long orderCount = 0L;
        long orderAmount = 0L;
        if (!metrics.isEmpty() && metrics.get(0) != null) {
            orderCount = ((Number) metrics.get(0)[0]).longValue();
            orderAmount = ((Number) metrics.get(0)[1]).longValue();
        }

        // 2. 어제 실제 DB 카운트 조회
        long memberJoinCount = memberRepository.countByCreatedAtBetween(startOfYesterday, endOfYesterday);
        long visitorCount = visitorService.getVisitorCount(LocalDate.now().minusDays(1));
        long qnaCount = qnaRepository.countByCreatedAtBetween(startOfYesterday, endOfYesterday);

        return DashboardStatsDTO.builder()
                .orderCount(orderCount)
                .orderAmount(orderAmount)
                .memberJoinCount(memberJoinCount) // <-- 진짜 어제 가입자 수!
                .visitorCount(visitorCount)       // <-- 진짜 어제 방문자 수!
                .qnaCount(qnaCount)               // <-- 진짜 어제 문의글 수!
                .build();
    }

    // ==========================================
    // [대시보드] 최근 5일 주문/결제/취소 통계
    // ==========================================
    @Override
    public List<DailySummaryDTO> getRecent5DaysSummary() {
        LocalDateTime startDate = LocalDate.now().minusDays(4).atStartOfDay();
        List<Object[]> results = orderRepository.getDailySummary(startDate);

        Map<String, DailySummaryDTO> summaryMap = new HashMap<>();
        for (Object[] row : results) {
            String date = (String) row[0];
            long orderCnt = ((Number) row[1]).longValue();
            long payCnt = ((Number) row[2]).longValue();
            long cancelCnt = ((Number) row[3]).longValue();
            summaryMap.put(date, new DailySummaryDTO(date, orderCnt, payCnt, cancelCnt));
        }

        List<DailySummaryDTO> dailyList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 4; i >= 0; i--) {
            String dateStr = LocalDate.now().minusDays(i).format(formatter);
            dailyList.add(summaryMap.getOrDefault(dateStr, new DailySummaryDTO(dateStr, 0L, 0L, 0L)));
        }

        return dailyList;
    }

    // ==========================================
    // [대시보드] 5일간 카테고리별 매출 (Top 3 + 기타)
    // ==========================================
    @Override
    public List<TopSalesDTO> getTopSalesCategories(int days) {
        LocalDateTime startDate = LocalDate.now().minusDays(days - 1).atStartOfDay();
        List<Object[]> results = orderDetailsRepository.getTopSalesByCategory(startDate);

        if (results.isEmpty()) return Collections.emptyList();

        long totalSum = 0;
        List<TopSalesDTO> rawList = new ArrayList<>();

        for (Object[] row : results) {
            String catName = row[0] != null ? (String) row[0] : "기타 카테고리";
            long sales = ((Number) row[1]).longValue();
            totalSum += sales;
            rawList.add(new TopSalesDTO(catName, sales, 0.0));
        }

        if (totalSum == 0) return Collections.emptyList();

        List<TopSalesDTO> topList = new ArrayList<>();
        long otherSales = 0;

        for (int i = 0; i < rawList.size(); i++) {
            TopSalesDTO dto = rawList.get(i);
            if (i < 3) {
                double percentage = Math.round((dto.getTotalSales() * 1000.0) / totalSum) / 10.0;
                dto.setPercentage(percentage);
                topList.add(dto);
            } else {
                otherSales += dto.getTotalSales();
            }
        }

        if (otherSales > 0) {
            double otherPercentage = Math.round((otherSales * 1000.0) / totalSum) / 10.0;
            topList.add(new TopSalesDTO("기타", otherSales, otherPercentage));
        }

        return topList;
    }
}
