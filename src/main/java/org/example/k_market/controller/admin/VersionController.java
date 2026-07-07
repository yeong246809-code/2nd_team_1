package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.dto.VersionDTO;
import org.example.k_market.service.admin.VersionService;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Log4j2
@Controller
@RequestMapping("/admin/config")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    // 1. 목록 화면 출력
    @GetMapping("/version")
    public String versionList(Model model) {
        // "versions"라는 이름으로 HTML에 데이터 전달 (th:each="ver : ${versions}" 와 매핑)
        List<VersionDTO> versions = versionService.getVersionList();
        model.addAttribute("versions", versions);

        // 실제 HTML 파일이 위치한 경로로 리턴 (templates/admin/config/version.html 기준)
        return "admin/config/version";
    }

    // 2. 버전 등록
    @PostMapping("/version")
    public String insertVersion(VersionDTO versionDTO) {
        // HTML 폼의 name 속성(versionCode, changeLog)이 VersionDTO에 자동 바인딩됩니다.
        versionService.insertVersion(versionDTO);

        // 등록 후 목록 페이지로 리다이렉트
        return "redirect:/admin/config/version";
    }

    // 3. 선택한 데이터 삭제
    @PostMapping("/version/delete")
    public String deleteVersions(@RequestParam("ids") List<Long> ids) {
        // HTML의 체크박스 name="ids" 값이 배열/리스트로 넘어옵니다.
        if (ids != null && !ids.isEmpty()) {
            versionService.deleteVersions(ids);
        }

        // 삭제 후 목록 페이지로 리다이렉트
        return "redirect:/admin/config/version";
    }
}
