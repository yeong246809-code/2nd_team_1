package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.SiteConfigDTO;
import org.example.k_market.service.admin.SiteConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/admin/config/basic")
@RequiredArgsConstructor
public class SiteConfigController {

    private final SiteConfigService siteConfigService;

    // 1. 설정 화면 출력 (GET)
    @GetMapping
    public String basicConfigPage(Model model) {
        SiteConfigDTO config = siteConfigService.getSiteConfig();
        String latestVersion = siteConfigService.getLatestVersionCode();

        model.addAttribute("config", config);
        model.addAttribute("latestVersion", latestVersion);

        return "admin/config/basic";
    }

    // 2. 사이트 폼 Update
    @PostMapping("/site")
    public String updateSite(SiteConfigDTO siteConfigDTO, RedirectAttributes redirectAttributes) {
        siteConfigService.updateSite(siteConfigDTO);
        redirectAttributes.addFlashAttribute("message", "사이트 정보가 수정되었습니다.");
        return "redirect:/admin/config/basic";
    }

    // 3. 로고 파일 업로드 Update (Multipart)
    @PostMapping("/logo")
    public String updateLogos(
            @RequestParam(value = "headerLogoFile", required = false) MultipartFile headerLogoFile,
            @RequestParam(value = "footerLogoFile", required = false) MultipartFile footerLogoFile,
            @RequestParam(value = "faviconFile", required = false) MultipartFile faviconFile,
            RedirectAttributes redirectAttributes) {

        try {
            siteConfigService.updateLogos(headerLogoFile, footerLogoFile, faviconFile);
            redirectAttributes.addFlashAttribute("message", "로고 정보가 수정되었습니다.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("message", "파일 업로드 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/config/basic";
    }

    // 4. 기업 정보 Update
    @PostMapping("/company")
    public String updateCompany(SiteConfigDTO siteConfigDTO, RedirectAttributes redirectAttributes) {
        siteConfigService.updateCompany(siteConfigDTO);
        redirectAttributes.addFlashAttribute("message", "기업 정보가 수정되었습니다.");
        return "redirect:/admin/config/basic";
    }

    // 5. 고객센터 정보 Update
    @PostMapping("/cs")
    public String updateCs(SiteConfigDTO siteConfigDTO, RedirectAttributes redirectAttributes) {
        siteConfigService.updateCs(siteConfigDTO);
        redirectAttributes.addFlashAttribute("message", "고객센터 정보가 수정되었습니다.");
        return "redirect:/admin/config/basic";
    }

    // 6. 카피라이트 Update
    @PostMapping("/copyright")
    public String updateCopyright(SiteConfigDTO siteConfigDTO, RedirectAttributes redirectAttributes) {
        siteConfigService.updateCopyright(siteConfigDTO);
        redirectAttributes.addFlashAttribute("message", "카피라이트 정보가 수정되었습니다.");
        return "redirect:/admin/config/basic";
    }
}
