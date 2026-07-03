package org.example.k_market.dao;

import org.example.k_market.dto.OrderDTO;
import org.example.k_market.entity.Order;
import org.example.k_market.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class OrderDAO {

    private final OrderRepository orderRepository;

    public OrderDTO save(OrderDTO dto) {
        // dto가 null이면 NullPointerException 발생 가능: Service에서 null 체크 후 호출하는 것을 권장
        Order entity = dto.toEntity();
        Order savedEntity = orderRepository.save(entity);
        return savedEntity.toDTO();
    }

    public Optional<OrderDTO> findById(Long orderNo) {
        // PK 타입이 엔티티의 @Id 타입과 다르면 컴파일 오류 발생
        return orderRepository.findById(orderNo)
                .map(Order::toDTO);
    }

    public List<OrderDTO> findAll() {
        return orderRepository.findAll()
                .stream()
                // Entity 내부 연관관계가 LAZY이면 toDTO() 호출 시점에 LazyInitializationException 가능
                .map(Order::toDTO)
                .toList();
    }

    public void deleteById(Long orderNo) {
        // 존재하지 않는 PK 삭제를 막기 위해 existsById로 먼저 확인
        if (orderRepository.existsById(orderNo)) {
            orderRepository.deleteById(orderNo);
        }
    }
}
