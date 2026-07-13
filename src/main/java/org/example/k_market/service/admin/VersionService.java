package org.example.k_market.service.admin;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.dto.PageResponseDTO;
import org.example.k_market.dto.VersionDTO;
import org.example.k_market.entity.Version;
import org.example.k_market.repository.VersionRepository;
import org.example.k_market.security.MyUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Service
public class VersionService {

    private final VersionRepository versionRepository;

    // 1. 화면 출력 (페이징 적용: page 번호를 받아서 PageResponseDTO로 반환)
    public PageResponseDTO<VersionDTO> getVersionList(int pg) {
        // JPA는 페이지 번호가 0부터 시작하므로 pg - 1, 한 페이지에 5개씩 조회(size: 5)
        Pageable pageable = PageRequest.of(pg - 1, 5);

        // Repository에 pageable 객체 전달!
        Page<VersionDTO> pageResult = versionRepository.findAllVersionsWithUserId(pageable);

        // blockSize를 5로 지정하여 반환 (1~5, 6~10 페이지 블록)
        return new PageResponseDTO<>(pageResult, 5);
    }

    public String getLatestVersionCode() {
        return versionRepository.findAll().stream()
                .map(Version::getVersionCode)
                .filter(code -> code != null && !code.isBlank())
                // VersionCodeComparator가 외부에 있거나 정의되어 있어야 합니다.
                // .max(VersionCodeComparator.INSTANCE)
                .max(Comparator.naturalOrder()) // 임시로 기본 정렬 사용 시
                .orElse(null);
    }

    // 2. 버전 등록 (로그인 정보 반영)
    @Transactional
    public void insertVersion(VersionDTO versionDTO) {
        if (versionDTO.getCreatedAt() == null) {
            versionDTO.setCreatedAt(LocalDateTime.now());
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof MyUserDetails) {
            MyUserDetails userDetails = (MyUserDetails) principal;
            int loggedInMemberNo = userDetails.getUser().getMemberNo();
            versionDTO.setMemberNo(loggedInMemberNo);
        } else {
            throw new RuntimeException("인증된 사용자 정보(MyUserDetails)를 찾을 수 없습니다.");
        }

        Version version = versionDTO.toEntity();
        versionRepository.save(version);
    }

    // 3. 선택한 데이터 삭제
    @Transactional
    public void deleteVersions(List<Long> ids) {
        versionRepository.deleteAllById(ids);
    }
}