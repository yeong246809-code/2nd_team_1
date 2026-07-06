package org.example.k_market.service.admin;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.dao.VersionDAO;
import org.example.k_market.dto.VersionDTO;
import org.example.k_market.entity.Version;
import org.example.k_market.repository.VersionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Service
public class VersionService {
    private final VersionDAO dao;
    private final VersionRepository repository;

    public List<VersionDTO> getAll() {
        List<Version> entityList = repository.findAllWithMember();

        return entityList.stream()
                .map(Version::toDTO) // 아주 깔끔해집니다!
                .toList();
    }

    public void insertVersion(VersionDTO versionDTO) {

        Version entity = versionDTO.toEntity();

        // DB에 저장 (JPA가 INSERT 쿼리를 날려줍니다)
        repository.save(entity);

        log.info("버전 등록 완료: {}", entity);
    }
}
