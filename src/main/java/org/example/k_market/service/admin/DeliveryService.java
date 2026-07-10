package org.example.k_market.service.admin;

import org.example.k_market.dto.DeliveriesDTO;

public interface DeliveryService {
    // 배송 정보 저장 및 주문 상태 변경
    void registerDelivery(DeliveriesDTO deliveryDTO);
}