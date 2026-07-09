package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.OrderDTO;
import org.example.k_market.dto.OrderDetailsDTO;
import org.example.k_market.entity.Order;
import org.example.k_market.entity.OrderDetails;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.MemberRepository;
import org.example.k_market.repository.OrderDetailsRepository;
import org.example.k_market.repository.OrderRepository;
import org.example.k_market.repository.UsersRepository;
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
        // 1. 주문 기본 정보 조회
        Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        OrderDTO dto = order.toDTO();

        // 2. 회원 정보 세팅
        usersRepository.findByMemberNo(order.getMemberNo()).ifPresent(u -> dto.setId(u.getId()));
        memberRepository.findById(order.getMemberNo()).ifPresent(m -> dto.setMemberName(m.getName()));

        // 3. 주문 상세(order_details) 조회
        // 서비스 로직에서 안전하게 처리
        List<OrderDetails> details = orderDetailsRepository.findByOrderNo(orderNo);
        if (details == null) {
            // 리스트가 null이면 빈 리스트로 초기화하여 에러 방지
            details = new ArrayList<>();
        }

        List<OrderDetailsDTO> detailDTOs = details.stream().map(detail -> {
            OrderDetailsDTO dDto = new OrderDetailsDTO();
            dDto.setProductNo(detail.getProductNo());
            dDto.setPrice(detail.getPrice());
            dDto.setDiscountPrice(detail.getDiscountPrice());
            dDto.setQuantity(detail.getQuantity());
            // 가격 계산
            dDto.setTotalPaymentPrice((detail.getPrice() * detail.getQuantity()) - detail.getDiscountPrice());
            return dDto;
        }).toList();

        dto.setOrderItems(detailDTOs); // OrderDTO의 필드명에 맞춰 설정
        return dto;
    }
}
