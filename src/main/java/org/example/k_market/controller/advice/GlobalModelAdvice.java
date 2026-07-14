package org.example.k_market.controller.advice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.k_market.repository.CartRepository;
import org.example.k_market.security.MyUserDetails;
import org.example.k_market.service.admin.SiteConfigService;
import org.example.k_market.service.admin.VersionService;
import org.example.k_market.service.member.MyBannerService;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final SiteConfigService siteConfigService;
    private final VersionService versionService;
    private final MyBannerService myBannerService;
    private final CartRepository cartRepository;

    @ModelAttribute
    public void addGlobalModelAttributes(Model model, HttpServletRequest request, Authentication authentication) {
        if (!model.containsAttribute("siteConfig")) {
            model.addAttribute("siteConfig", siteConfigService.getSiteConfig());
        }
        if (!model.containsAttribute("latestVersion")) {
            model.addAttribute("latestVersion", versionService.getLatestVersionCode());
        }
        String uri = request.getRequestURI();
        if (uri != null && uri.contains("/my/") && !model.containsAttribute("myBanners")) {
            model.addAttribute("myBanners", myBannerService.findMyPageBanners());
        }
        long cartItemCount = 0;
        if (authentication != null && authentication.getPrincipal() instanceof MyUserDetails userDetails) {
            cartItemCount = cartRepository.sumQuantityByMemberNo(userDetails.getUser().getMemberNo());
        }
        model.addAttribute("cartItemCount", cartItemCount);
    }
}
