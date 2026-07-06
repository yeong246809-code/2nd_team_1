package org.example.k_market.dao;

import org.example.k_market.dto.OrderDetailsDTO;
import org.example.k_market.entity.OrderDetails;
import org.example.k_market.repository.OrderDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class OrderDetailsDAO {

    private final OrderDetailsRepository orderDetailsRepository;

    public OrderDetailsDTO save(OrderDetailsDTO dto) {
        // dto가 null이면 NullPointerException 발생 가능: Service에서 null 체크 후 호출하는 것을 권장
        OrderDetails entity = dto.toEntity();
        OrderDetails savedEntity = orderDetailsRepository.save(entity);
        return savedEntity.toDTO();
    }

    public Optional<OrderDetailsDTO> findById(Long orderDetailNo) {
        // PK 타입이 엔티티의 @Id 타입과 다르면 컴파일 오류 발생
        return orderDetailsRepository.findById(orderDetailNo)
                .map(OrderDetails::toDTO);
    }

    public List<OrderDetailsDTO> findAll() {
        return orderDetailsRepository.findAll()
                .stream()
                // Entity 내부 연관관계가 LAZY이면 toDTO() 호출 시점에 LazyInitializationException 가능
                .map(OrderDetails::toDTO)
                .toList();
    }

    public void deleteById(Long orderDetailNo) {
        // 존재하지 않는 PK 삭제를 막기 위해 existsById로 먼저 확인
        if (orderDetailsRepository.existsById(orderDetailNo)) {
            orderDetailsRepository.deleteById(orderDetailNo);
        }
    }
}
