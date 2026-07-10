package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.DeliveriesDTO;
import org.example.k_market.entity.Order;
import org.example.k_market.repository.DeliveriesRepository;
import org.example.k_market.repository.OrderRepository;
import org.example.k_market.service.admin.DeliveryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveriesRepository deliveryRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void registerDelivery(DeliveriesDTO deliveryDTO) {
        // 1. 배송 정보 저장
        deliveryRepository.save(deliveryDTO.toEntity());

        // 2. 주문 상태를 '배송중'으로 변경
        Order order = orderRepository.findById((int) deliveryDTO.getOrderNo())
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다."));

        order.setStatus("배송중");
        // orderRepository.save(order); // @Transactional이 있다면 자동 반영(Dirty Checking)됨
    }
}