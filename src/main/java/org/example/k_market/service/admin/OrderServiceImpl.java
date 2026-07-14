package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.DeliveryDTO;
import org.example.k_market.dto.OrderDTO;
import org.example.k_market.dto.OrderDetailsDTO;
import org.example.k_market.entity.Deliveries;
import org.example.k_market.entity.Order;
import org.example.k_market.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final DeliveryRepository deliveriesRepository;
    private final UsersRepository usersRepository;
    private final ProductRepository productRepository;

    // 1. 주문 목록 조회 (검색 기능 포함)
    @Override
    public Page<OrderDTO> findAllOrders(Pageable pageable) {
        // join fetch를 사용하여 N+1 문제를 방지하고 성능 최적화
        return orderRepository.findAllWithJoin(pageable).map(order -> {
            OrderDTO dto = order.toDTO();
            // User ID가 필요한 경우 매핑
            if (order.getUser() != null) {
                dto.setId(order.getUser().getId());
            }
            return dto;
        });
    }

    // 2. 주문 상세 조회
    public OrderDTO findOrderDetail(int orderNo) {
        // 1. 주문 조회 (이제 엔티티에 배송 정보가 자동으로 담겨서 나옵니다)
        Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        // 2. DTO 변환 (이미 toDTO에서 모든 필드를 세팅하도록 수정)
        OrderDTO dto = order.toDTO();

        // 3. 상품 상세 정보 조립
        dto.setOrderItems(orderDetailsRepository.findByOrderNo(orderNo).stream().map(detail -> {
            OrderDetailsDTO item = detail.toDTO();
            productRepository.findById(detail.getProductNo())
                    .ifPresent(p -> item.setProdName(p.getName()));
            item.setTotalPaymentPrice((detail.getPrice() * detail.getQuantity()) - detail.getDiscountPrice());
            return item;
        }).toList());

        return dto;
    }

    @Override
    public OrderDTO findSellerOrderDetail(long shopNo, int orderNo) {
        Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        var details = orderDetailsRepository.findByOrderNoAndShopNo(orderNo, shopNo);
        if (details.isEmpty()) {
            throw new org.springframework.security.access.AccessDeniedException("다른 판매자의 주문입니다.");
        }
        OrderDTO dto = toSellerOrderDTO(order, details);
        dto.setOrderItems(details.stream().map(detail -> {
            OrderDetailsDTO item = detail.toDTO();
            productRepository.findById(detail.getProductNo()).ifPresent(p -> item.setProdName(p.getName()));
            item.setTotalPaymentPrice((detail.getPrice() * detail.getQuantity()) - detail.getDiscountPrice());
            return item;
        }).toList());
        return dto;
    }

    // 3. 주문 상태 변경
    @Transactional
    @Override
    public void updateOrderStatus(int orderNo, String status) {
        Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        order.setStatus(status);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void updateSellerOrderStatus(long shopNo, int orderNo, String status) {
        int updated = orderDetailsRepository.updateStatusByOrderNoAndShopNo(orderNo, shopNo, status);
        if (updated == 0) {
            throw new org.springframework.security.access.AccessDeniedException("다른 판매자의 주문입니다.");
        }
    }

    public Page<OrderDTO> findOrderList(Pageable pageable, String searchType, String keyword) {
        return orderRepository.findAllWithJoin(pageable).map(Order::toDTO);
    }

    @Override
    public Page<OrderDTO> findSellerOrderList(long shopNo, Pageable pageable, String searchType, String keyword) {
        return orderRepository.findSellerOrders(shopNo, searchType, keyword, pageable)
                .map(order -> toSellerOrderDTO(
                        order, orderDetailsRepository.findByOrderNoAndShopNo(order.getOrderNo(), shopNo)));
    }

    private OrderDTO toSellerOrderDTO(Order order, java.util.List<org.example.k_market.entity.OrderDetails> details) {
        OrderDTO dto = order.toDTO();
        dto.setTotalProductPrice(details.stream().mapToInt(d -> d.getPrice() * d.getQuantity()).sum());
        dto.setTotalDiscountPrice(details.stream().mapToInt(org.example.k_market.entity.OrderDetails::getDiscountPrice).sum());
        dto.setTotalShippingFee(details.stream().mapToInt(org.example.k_market.entity.OrderDetails::getShippingFee).sum());
        dto.setTotalPaymentPrice(details.stream()
                .mapToInt(d -> d.getPrice() * d.getQuantity() - d.getDiscountPrice() + d.getShippingFee()).sum());
        if (!details.isEmpty()) dto.setStatus(details.get(0).getStatus());
        return dto;
    }

    @Override
    public boolean updateOrderStatus(String orderNo, String status) {
        return false;
    }

    @Override
    public OrderDTO getOrderDetail(int orderNo) {
        return null;
    }

    @Transactional
    public void registerDelivery(DeliveryDTO dto) {
        // 1. DTO를 엔티티로 변환
        Deliveries delivery = dto.toEntity();

        // 2. 배송 테이블에 저장
        deliveriesRepository.save(delivery);

        // 3. 주문 상태도 '배송중'으로 변경
        Order order = orderRepository.findById((int)dto.getOrderNo()).orElseThrow();
        order.setStatus("결제완료(배송중)");
    }
}
