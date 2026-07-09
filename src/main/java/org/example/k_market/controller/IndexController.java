package org.example.k_market.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.service.admin.SiteConfigService;
import org.example.k_market.service.admin.VersionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
@RequiredArgsConstructor
public class IndexController {

    private final SiteConfigService siteConfigService;
    private final VersionService versionService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("siteConfig", siteConfigService.getSiteConfig());
        model.addAttribute("latestVersion", versionService.getLatestVersionCode());
        return "index";
    }
}
