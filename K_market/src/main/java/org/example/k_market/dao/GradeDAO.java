package org.example.k_market.dao;

import org.example.k_market.dto.GradeDTO;
import org.example.k_market.entity.Grade;
import org.example.k_market.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class GradeDAO {

    private final GradeRepository gradeRepository;

    public GradeDTO save(GradeDTO dto) {
        // dto가 null이면 NullPointerException 발생 가능: Service에서 null 체크 후 호출하는 것을 권장
        Grade entity = dto.toEntity();
        Grade savedEntity = gradeRepository.save(entity);
        return savedEntity.toDTO();
    }

    public Optional<GradeDTO> findById(Integer gradeNo) {
        // PK 타입이 엔티티의 @Id 타입과 다르면 컴파일 오류 발생
        return gradeRepository.findById(gradeNo)
                .map(Grade::toDTO);
    }

    public List<GradeDTO> findAll() {
        return gradeRepository.findAll()
                .stream()
                // Entity 내부 연관관계가 LAZY이면 toDTO() 호출 시점에 LazyInitializationException 가능
                .map(Grade::toDTO)
                .toList();
    }

    public void deleteById(Integer gradeNo) {
        // 존재하지 않는 PK 삭제를 막기 위해 existsById로 먼저 확인
        if (gradeRepository.existsById(gradeNo)) {
            gradeRepository.deleteById(gradeNo);
        }
    }
}
