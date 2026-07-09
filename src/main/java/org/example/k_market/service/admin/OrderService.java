package org.example.k_market.service.admin;

import org.example.k_market.dto.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // 이 패키지로 통일
import org.springframework.stereotype.Service;

@Service
public interface OrderService {
    Page<OrderDTO> findOrderList(Pageable pageable, String searchType, String keyword);
    boolean updateOrderStatus(String orderNo, String status);

    // 상세 주문 정보 조회 메서드 추가
    OrderDTO getOrderDetail(int orderNo);
}