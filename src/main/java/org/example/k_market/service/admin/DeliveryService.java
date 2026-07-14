package org.example.k_market.service.admin;

import org.example.k_market.dto.DashboardStatsDTO;
import org.example.k_market.dto.DeliveryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public interface DeliveryService {
    // 배송 정보 저장 및 주문 상태 변경
    void registerDelivery(DeliveryDTO deliveryDTO);

    List<DeliveryDTO> getAllDeliveries();

    Page<DeliveryDTO> getDeliveryList();

    @Transactional(readOnly = true)
    Page<DeliveryDTO> getDeliveryList(Pageable pageable);
    Page<DeliveryDTO> getSellerDeliveryList(long shopNo, Pageable pageable);

    DeliveryDTO getDetailByTrackingNumber(String trackingNumber);

    DashboardStatsDTO getTodayDashboardStats();

    // ==========================================
    // [대시보드] 어제 기준 주요지표
    // ==========================================
    DashboardStatsDTO getYesterdayDashboardStats();
}
