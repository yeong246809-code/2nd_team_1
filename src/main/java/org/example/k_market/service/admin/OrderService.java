package org.example.k_market.service.admin;

import org.example.k_market.dto.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;

@Service
public interface OrderService {
    Page<OrderDTO> findOrderList(org.springframework.data.domain.Pageable pageable, String searchType, String keyword);

    Page<OrderDTO> findOrderList(Pageable pageable, String searchType, String keyword);
    boolean updateOrderStatus(String orderNo, String status);
}
