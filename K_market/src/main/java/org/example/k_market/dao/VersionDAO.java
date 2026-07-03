package org.example.k_market.dao;

import org.example.k_market.dto.VersionDTO;
import org.example.k_market.entity.Version;
import org.example.k_market.repository.VersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class VersionDAO {

    private final VersionRepository versionRepository;

    public VersionDTO save(VersionDTO dto) {
        // dto가 null이면 NullPointerException 발생 가능: Service에서 null 체크 후 호출하는 것을 권장
        Version entity = dto.toEntity();
        Version savedEntity = versionRepository.save(entity);
        return savedEntity.toDTO();
    }

    public Optional<VersionDTO> findById(Long id) {
        // PK 타입이 엔티티의 @Id 타입과 다르면 컴파일 오류 발생
        return versionRepository.findById(id)
                .map(Version::toDTO);
    }

    public List<VersionDTO> findAll() {
        return versionRepository.findAll()
                .stream()
                // Entity 내부 연관관계가 LAZY이면 toDTO() 호출 시점에 LazyInitializationException 가능
                .map(Version::toDTO)
                .toList();
    }

    public void deleteById(Long id) {
        // 존재하지 않는 PK 삭제를 막기 위해 existsById로 먼저 확인
        if (versionRepository.existsById(id)) {
            versionRepository.deleteById(id);
        }
    }
}
