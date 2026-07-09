package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.OrderDTO;
import org.example.k_market.dto.OrderDetailsDTO;
import org.example.k_market.entity.Order;
import org.example.k_market.entity.OrderDetails;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final UsersRepository usersRepository; // 아이디(ID)용
    private final MemberRepository memberRepository; // 이름(Name)용
    private final ProductRepository productRepository;
    private final DeliveriesRepository deliveriesRepository;

    @Override
    public Page<OrderDTO> findOrderList(Pageable pageable, String searchType, String keyword) {
        Page<Order> orderPage = orderRepository.findAll(pageable);

        // 1. 모든 유저를 메모리에 올림 (데이터가 너무 많지 않다면 효과적)
        List<Users> allUsers = usersRepository.findAll();

        return orderPage.map(order -> {
            OrderDTO dto = order.toDTO();

            // 2. 서비스 내에서 직접 매칭
            allUsers.stream()
                    .filter(u -> u.getMemberNo() == order.getMemberNo())
                    .findFirst()
                    .ifPresent(user -> dto.setId(user.getId()));

            // 이름은 기존대로 memberRepository 사용
            memberRepository.findById(order.getMemberNo()).ifPresent(m -> dto.setMemberName(m.getName()));

            return dto;
        });
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

    @Override
    public OrderDTO getOrderDetail(int orderNo) {
        // 1. 주문 조회
        Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        OrderDTO dto = order.toDTO();

        // 2. 주문자명 (Order 엔티티 우선)
        dto.setMemberName(order.getOrderName() != null ? order.getOrderName() : "비회원");

        // 3. 회원 ID (Users)
        usersRepository.findByMemberNo(order.getMemberNo())
                .ifPresent(u -> dto.setId(u.getId()));

        // 4. 배송 정보 (Deliveries 테이블에서 조회 - 핵심!)
        deliveriesRepository.findByOrderNo(orderNo).ifPresent(d -> {
            dto.setRecipientName(d.getRecipientName());
            dto.setRecipientPhone(d.getRecipientPhone());
            dto.setBaseAddress(d.getBaseAddress());
        });

        // 5. 상품 상세 (OrderDetails)
        List<OrderDetails> details = orderDetailsRepository.findByOrderNo(orderNo);
        if (details == null) details = new ArrayList<>();

        List<OrderDetailsDTO> detailDTOs = details.stream().map(detail -> {
            OrderDetailsDTO dDto = new OrderDetailsDTO();
            dDto.setProductNo(detail.getProductNo());
            dDto.setPrice(detail.getPrice());
            dDto.setDiscountPrice(detail.getDiscountPrice());
            dDto.setQuantity(detail.getQuantity());
            dDto.setTotalPaymentPrice((detail.getPrice() * detail.getQuantity()) - detail.getDiscountPrice());

            productRepository.findById(detail.getProductNo())
                    .ifPresent(p -> dDto.setProdName(p.getName()));
            return dDto;
        }).toList();

        dto.setOrderItems(detailDTOs);
        return dto;
    }
}
