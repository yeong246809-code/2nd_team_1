package org.example.k_market.dao;

import org.example.k_market.dto.SiteConfigDTO;
import org.example.k_market.entity.SiteConfig;
import org.example.k_market.repository.SiteConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class SiteConfigDAO {

    private final SiteConfigRepository siteConfigRepository;

    public SiteConfigDTO save(SiteConfigDTO dto) {
        // dto가 null이면 NullPointerException 발생 가능: Service에서 null 체크 후 호출하는 것을 권장
        SiteConfig entity = dto.toEntity();
        SiteConfig savedEntity = siteConfigRepository.save(entity);
        return savedEntity.toDTO();
    }

    public Optional<SiteConfigDTO> findById(Integer id) {
        // PK 타입이 엔티티의 @Id 타입과 다르면 컴파일 오류 발생
        return siteConfigRepository.findById(id)
                .map(SiteConfig::toDTO);
    }

    public List<SiteConfigDTO> findAll() {
        return siteConfigRepository.findAll()
                .stream()
                // Entity 내부 연관관계가 LAZY이면 toDTO() 호출 시점에 LazyInitializationException 가능
                .map(SiteConfig::toDTO)
                .toList();
    }

    public void deleteById(Integer id) {
        // 존재하지 않는 PK 삭제를 막기 위해 existsById로 먼저 확인
        if (siteConfigRepository.existsById(id)) {
            siteConfigRepository.deleteById(id);
        }
    }
}
