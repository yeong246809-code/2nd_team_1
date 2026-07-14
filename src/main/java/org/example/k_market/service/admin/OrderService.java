package org.example.k_market.service.admin;

import org.example.k_market.dto.DailySummaryDTO;
import org.example.k_market.dto.DashboardStatsDTO;
import org.example.k_market.dto.OrderDTO;
import org.example.k_market.dto.TopSalesDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // 이 패키지로 통일
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public interface OrderService {
    // 주문 전체 목록 조회
    Page<OrderDTO> findAllOrders(Pageable pageable);
    Page<OrderDTO> findOrderList(Pageable pageable, String searchType, String keyword);

    // 주문 상세 조회 (두 메서드 중 하나만 남기고 나머지는 구현체에서 호출하도록 유도해도 됩니다)
    OrderDTO findOrderDetail(int orderNo);
    OrderDTO getOrderDetail(int orderNo); // 구현체에서 findOrderDetail(orderNo) 호출

    // 주문 상태 변경
    void updateOrderStatus(int orderNo, String status);
    boolean updateOrderStatus(String orderNo, String status); // 구현체에서 위 메서드 호출

    // 대시보드 통계 메서드
    DashboardStatsDTO getTodayDashboardStats();
    DashboardStatsDTO getYesterdayDashboardStats();
    List<DailySummaryDTO> getRecent5DaysSummary();
    List<TopSalesDTO> getTopSalesCategories(int days);
}
