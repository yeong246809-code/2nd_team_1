package org.example.k_market.controller.advice;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.SiteConfigDTO;
import org.example.k_market.service.admin.SiteConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class FaviconController {

    private final SiteConfigService siteConfigService;

    @GetMapping("/favicon.ico")
    public String favicon() {
        SiteConfigDTO config = siteConfigService.getSiteConfig();
        String faviconName = config.getFavicon_stored();

        // 1. 관리자가 파비콘을 업로드 해둔 경우
        if (StringUtils.hasText(faviconName)) {
            // 브라우저 주소창은 /favicon.ico 그대로 유지되지만,
            // 내부적으로는 /uploads/저장된파비콘이름.ico 파일을 보여줍니다.
            return "forward:/uploads/" + faviconName;
        }

        // 2. 파비콘을 아직 업로드하지 않은 경우 (에러 방지용)
        // resources/static 폴더 안에 빈 파일이나 임시 기본 아이콘을 하나 넣어두시면 좋습니다.
        return "forward:/default-favicon.ico";
    }
}