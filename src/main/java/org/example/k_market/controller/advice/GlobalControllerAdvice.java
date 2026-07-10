package org.example.k_market.controller.advice;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.SiteConfigDTO;
import org.example.k_market.service.admin.SiteConfigService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final SiteConfigService siteConfigService;

    // 모든 뷰(Thymeleaf)에서 'config'라는 이름으로 SiteConfigDTO를 사용할 수 있게 됨
    @ModelAttribute("config")
    public SiteConfigDTO globalSiteConfig() {
        return siteConfigService.getSiteConfig();
    }
}