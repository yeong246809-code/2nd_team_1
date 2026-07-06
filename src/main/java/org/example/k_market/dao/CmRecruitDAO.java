package org.example.k_market.dao;

import org.example.k_market.dto.CmRecruitDTO;
import org.example.k_market.entity.CmRecruit;
import org.example.k_market.repository.CmRecruitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class CmRecruitDAO {

    private final CmRecruitRepository cmRecruitRepository;

    public CmRecruitDTO save(CmRecruitDTO dto) {
        // dto가 null이면 NullPointerException 발생 가능: Service에서 null 체크 후 호출하는 것을 권장
        CmRecruit entity = dto.toEntity();
        CmRecruit savedEntity = cmRecruitRepository.save(entity);
        return savedEntity.toDTO();
    }

    public Optional<CmRecruitDTO> findById(Integer id) {
        // PK 타입이 엔티티의 @Id 타입과 다르면 컴파일 오류 발생
        return cmRecruitRepository.findById(id)
                .map(CmRecruit::toDTO);
    }

    public List<CmRecruitDTO> findAll() {
        return cmRecruitRepository.findAll()
                .stream()
                // Entity 내부 연관관계가 LAZY이면 toDTO() 호출 시점에 LazyInitializationException 가능
                .map(CmRecruit::toDTO)
                .toList();
    }

    public void deleteById(Integer id) {
        // 존재하지 않는 PK 삭제를 막기 위해 existsById로 먼저 확인
        if (cmRecruitRepository.existsById(id)) {
            cmRecruitRepository.deleteById(id);
        }
    }
}
