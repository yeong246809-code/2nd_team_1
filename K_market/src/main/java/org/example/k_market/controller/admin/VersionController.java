package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.dto.VersionDTO;
import org.example.k_market.repository.VersionRepository;
import org.example.k_market.security.MyUserDetails;
import org.example.k_market.service.admin.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Controller
public class VersionController {

    // @Autowired 대신 final을 붙이면 @RequiredArgsConstructor가 알아서 의존성을 주입해 줍니다. (실무 권장 방식)
    private final VersionService versionService;

    // 1. 버전 관리 페이지 (HTML) 보여주기
    @GetMapping("/admin/config/version")
    public String versionPage(Model model) {
        // 나중에 목록 출력이 필요하면 여기서 service.getVersions() 등을 호출해서 model에 담아줍니다.
        return "admin/config/version"; // src/main/resources/templates/admin/version.html
    }

    // 2. 버전 등록 처리 (모달창에서 fetch 로 전송된 데이터 받기)
    @ResponseBody // fetch 요청에 대한 응답이므로, HTML 파일 이름이 아닌 데이터를 반환해야 함
    @PostMapping("/admin/config/version")
    public ResponseEntity<?> registerVersion(

            @RequestBody VersionDTO versionDTO,
            @AuthenticationPrincipal MyUserDetails myUserDetails) { // ★ 시큐리티 세션 정보 가져오기

        log.info("버전 등록 폼에서 넘어온 데이터: " + versionDTO);

        try {
            // 1. 현재 로그인한 관리자의 정보(MemberNo) 꺼내기
            Integer currentMemberNo = myUserDetails.getUser().getMemberNo();

            // 2. DTO에 작성자(관리자) 회원번호 세팅
            versionDTO.setMemberNo(currentMemberNo);

            log.info("세팅된 memberNo 확인: " + versionDTO.getMemberNo());

            // 3. Service로 넘겨서 DB 저장 (버전 코드, 변경 내역, 작성자 번호)
            versionService.insertVersion(versionDTO);

            log.info("버전 등록 성공!");
            return ResponseEntity.ok().body("등록 성공");

        } catch (Exception e) {
            log.error("버전 등록 실패: ", e);
            return ResponseEntity.badRequest().body("등록 실패");
        }
    }
}
