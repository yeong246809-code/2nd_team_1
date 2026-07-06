package org.example.k_market.dao;

import org.example.k_market.dto.OrderClaimDTO;
import org.example.k_market.entity.OrderClaim;
import org.example.k_market.repository.OrderClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class OrderClaimDAO {

    private final OrderClaimRepository orderClaimRepository;

    public OrderClaimDTO save(OrderClaimDTO dto) {
        // dto가 null이면 NullPointerException 발생 가능: Service에서 null 체크 후 호출하는 것을 권장
        OrderClaim entity = dto.toEntity();
        OrderClaim savedEntity = orderClaimRepository.save(entity);
        return savedEntity.toDTO();
    }

    public Optional<OrderClaimDTO> findById(Long id) {
        // PK 타입이 엔티티의 @Id 타입과 다르면 컴파일 오류 발생
        return orderClaimRepository.findById(id)
                .map(OrderClaim::toDTO);
    }

    public List<OrderClaimDTO> findAll() {
        return orderClaimRepository.findAll()
                .stream()
                // Entity 내부 연관관계가 LAZY이면 toDTO() 호출 시점에 LazyInitializationException 가능
                .map(OrderClaim::toDTO)
                .toList();
    }

    public void deleteById(Long id) {
        // 존재하지 않는 PK 삭제를 막기 위해 existsById로 먼저 확인
        if (orderClaimRepository.existsById(id)) {
            orderClaimRepository.deleteById(id);
        }
    }
}
