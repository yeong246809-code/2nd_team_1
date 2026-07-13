package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.DeliveryDTO;
import org.example.k_market.entity.Deliveries;
import org.example.k_market.entity.Order;
import org.example.k_market.entity.OrderDetails;
import org.example.k_market.repository.DeliveryRepository;
import org.example.k_market.repository.OrderDetailsRepository;
import org.example.k_market.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;

    @Override
    @Transactional
    public void registerDelivery(DeliveryDTO deliveryDTO) {
        // 1. 배송 정보 저장
        deliveryRepository.save(deliveryDTO.toEntity());

        // 2. 주문 상태를 '배송중'으로 변경
        Order order = orderRepository.findById((int) deliveryDTO.getOrderNo())
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다."));

        order.setStatus("배송중");
        // orderRepository.save(order); // @Transactional이 있다면 자동 반영(Dirty Checking)됨
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
        return deliveryRepository.findAll(pageable).map(d -> {
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
        return null;
    }
}
