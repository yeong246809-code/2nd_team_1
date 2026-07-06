package org.example.k_market.dao;

import org.example.k_market.dto.DeliveriesDTO;
import org.example.k_market.entity.Deliveries;
import org.example.k_market.repository.DeliveriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class DeliveriesDAO {

    private final DeliveriesRepository deliveriesRepository;

    public DeliveriesDTO save(DeliveriesDTO dto) {
        // dto가 null이면 NullPointerException 발생 가능: Service에서 null 체크 후 호출하는 것을 권장
        Deliveries entity = dto.toEntity();
        Deliveries savedEntity = deliveriesRepository.save(entity);
        return savedEntity.toDTO();
    }

    public Optional<DeliveriesDTO> findById(Long deliverNo) {
        // PK 타입이 엔티티의 @Id 타입과 다르면 컴파일 오류 발생
        return deliveriesRepository.findById(deliverNo)
                .map(Deliveries::toDTO);
    }

    public List<DeliveriesDTO> findAll() {
        return deliveriesRepository.findAll()
                .stream()
                // Entity 내부 연관관계가 LAZY이면 toDTO() 호출 시점에 LazyInitializationException 가능
                .map(Deliveries::toDTO)
                .toList();
    }

    public void deleteById(Long deliverNo) {
        // 존재하지 않는 PK 삭제를 막기 위해 existsById로 먼저 확인
        if (deliveriesRepository.existsById(deliverNo)) {
            deliveriesRepository.deleteById(deliverNo);
        }
    }
}
