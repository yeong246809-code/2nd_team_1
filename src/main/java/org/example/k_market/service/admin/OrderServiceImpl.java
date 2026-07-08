package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.OrderDTO;
import org.example.k_market.entity.Order;
import org.example.k_market.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public Page<OrderDTO> findOrderList(Pageable pageable, String searchType, String keyword) {
        Page<Order> orderPage;

        // 검색 로직
        if (keyword != null && !keyword.isEmpty()) {
            try {
                int keywordInt = Integer.parseInt(keyword); // int로 변환
                orderPage = orderRepository.findByOrderNoContaining(keywordInt, pageable);
            } catch (NumberFormatException e) {
                // 숫자가 아닌 경우 전체 조회하거나 예외 처리
                orderPage = orderRepository.findAll(pageable);
            }
        } else {
            orderPage = orderRepository.findAll(pageable);
        }

        // Entity -> DTO 변환 (엔티티에 만든 toDTO() 메서드 활용)
        return orderPage.map(Order::toDTO);
    }

    @Override
    public Page<OrderDTO> findOrderList(java.awt.print.Pageable pageable, String searchType, String keyword) {
        return null;
    }

    @Transactional
    @Override
    public boolean updateOrderStatus(String orderNo, String status) {
        try {
            int id = Integer.parseInt(orderNo);
            Order order = orderRepository.findById((Integer) id)
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

            // Order 엔티티에 상태(status) 필드가 있다면 setter 또는 update 메서드 사용
            // order.setStatus(status);
            // orderRepository.save(order);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
