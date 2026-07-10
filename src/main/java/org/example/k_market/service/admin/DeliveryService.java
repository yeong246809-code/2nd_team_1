package org.example.k_market.service.admin;

import org.example.k_market.dto.DeliveryDTO;
import org.springframework.stereotype.Service;

@Service
public interface DeliveryService {
    // 배송 정보 저장 및 주문 상태 변경
    void registerDelivery(DeliveryDTO deliveryDTO);
}