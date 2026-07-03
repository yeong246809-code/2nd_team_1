package org.example.k_market.dao;

import org.example.k_market.dto.PointHistoryDTO;
import org.example.k_market.entity.PointHistory;
import org.example.k_market.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class PointHistoryDAO {

    private final PointHistoryRepository pointHistoryRepository;

    public PointHistoryDTO save(PointHistoryDTO dto) {
        // dto가 null이면 NullPointerException 발생 가능: Service에서 null 체크 후 호출하는 것을 권장
        PointHistory entity = dto.toEntity();
        PointHistory savedEntity = pointHistoryRepository.save(entity);
        return savedEntity.toDTO();
    }

    public Optional<PointHistoryDTO> findById(Long pointNo) {
        // PK 타입이 엔티티의 @Id 타입과 다르면 컴파일 오류 발생
        return pointHistoryRepository.findById(pointNo)
                .map(PointHistory::toDTO);
    }

    public List<PointHistoryDTO> findAll() {
        return pointHistoryRepository.findAll()
                .stream()
                // Entity 내부 연관관계가 LAZY이면 toDTO() 호출 시점에 LazyInitializationException 가능
                .map(PointHistory::toDTO)
                .toList();
    }

    public void deleteById(Long pointNo) {
        // 존재하지 않는 PK 삭제를 막기 위해 existsById로 먼저 확인
        if (pointHistoryRepository.existsById(pointNo)) {
            pointHistoryRepository.deleteById(pointNo);
        }
    }
}
