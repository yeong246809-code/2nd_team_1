package org.example.k_market.service.admin;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.dao.VersionDAO;
import org.example.k_market.dto.VersionDTO;
import org.example.k_market.entity.Version;
import org.example.k_market.repository.VersionRepository;
import org.example.k_market.security.MyUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Service
public class VersionService {

    private final VersionRepository versionRepository;

    // 1. 화면 출력 (작성자 ID가 포함된 목록 반환)
    public List<VersionDTO> getVersionList() {
        return versionRepository.findAllVersionsWithUserId();
    }

    // 2. 버전 등록 (로그인 정보 반영)
    @Transactional
    public void insertVersion(VersionDTO versionDTO) {
        // 등록일시가 비어있으면 현재 시간 세팅
        if (versionDTO.getCreatedAt() == null) {
            versionDTO.setCreatedAt(LocalDateTime.now());
        }

        // 시큐리티 컨텍스트에서 현재 로그인한 사용자 정보 가져오기
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 정상적으로 로그인된 MyUserDetails 객체인지 확인
        if (principal instanceof MyUserDetails) {
            MyUserDetails userDetails = (MyUserDetails) principal;

            // userDetails 안에 담아둔 Users 엔티티에서 memberNo 추출
            int loggedInMemberNo = userDetails.getUser().getMemberNo();
            versionDTO.setMemberNo(loggedInMemberNo);
        } else {
            // 시큐리티를 통과했는데 이 분기를 탄다면 인증 객체 설정에 문제가 있는 것입니다.
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