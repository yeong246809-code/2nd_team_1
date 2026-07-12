package org.example.k_market.controller.advice;

import lombok.RequiredArgsConstructor;
import org.example.k_market.service.admin.SiteConfigService;
import org.example.k_market.service.admin.VersionService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final SiteConfigService siteConfigService;
    private final VersionService versionService;

    @ModelAttribute
    public void addGlobalModelAttributes(Model model) {
        if (!model.containsAttribute("siteConfig")) {
            model.addAttribute("siteConfig", siteConfigService.getSiteConfig());
        }
        if (!model.containsAttribute("latestVersion")) {
            model.addAttribute("latestVersion", versionService.getLatestVersionCode());
        }
    }
}
