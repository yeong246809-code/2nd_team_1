package org.example.k_market.dao;

import org.example.k_market.dto.CmStoryDTO;
import org.example.k_market.entity.CmStory;
import org.example.k_market.repository.CmStoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class CmStoryDAO {

    private final CmStoryRepository cmStoryRepository;

    public CmStoryDTO save(CmStoryDTO dto) {
        // dto가 null이면 NullPointerException 발생 가능: Service에서 null 체크 후 호출하는 것을 권장
        CmStory entity = dto.toEntity();
        CmStory savedEntity = cmStoryRepository.save(entity);
        return savedEntity.toDTO();
    }

    public Optional<CmStoryDTO> findById(Integer id) {
        // PK 타입이 엔티티의 @Id 타입과 다르면 컴파일 오류 발생
        return cmStoryRepository.findById(id)
                .map(CmStory::toDTO);
    }

    public List<CmStoryDTO> findAll() {
        return cmStoryRepository.findAll()
                .stream()
                // Entity 내부 연관관계가 LAZY이면 toDTO() 호출 시점에 LazyInitializationException 가능
                .map(CmStory::toDTO)
                .toList();
    }

    public void deleteById(Integer id) {
        // 존재하지 않는 PK 삭제를 막기 위해 existsById로 먼저 확인
        if (cmStoryRepository.existsById(id)) {
            cmStoryRepository.deleteById(id);
        }
    }
}
