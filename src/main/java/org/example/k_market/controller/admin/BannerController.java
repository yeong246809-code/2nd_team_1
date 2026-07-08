package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.BannerDTO;
import org.example.k_market.service.admin.BannerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin/config")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping("/banner")
    public String bannerPage(Model model) {
        model.addAttribute("bannerList", bannerService.getAllBanners());
        return "admin/config/banner";
    }

    @PostMapping("/banner/add")
    public String addBanner(
            BannerDTO bannerDTO,
            @RequestParam(value = "file", required = false) MultipartFile file,
            RedirectAttributes redirectAttributes) {
        try {
            bannerService.registerBanner(bannerDTO, file);
            redirectAttributes.addFlashAttribute("message", "배너가 등록되었습니다.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("message", "배너 파일 업로드 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/config/banner";
    }

    @GetMapping("/banner/status")
    public String toggleStatus(@RequestParam("id") Long id) {
        bannerService.toggleStatus(id);
        return "redirect:/admin/config/banner";
    }

    @PostMapping("/banner/delete")
    public String deleteBanners(@RequestParam(value = "bannerIds", required = false) List<Long> bannerIds) {
        if (bannerIds != null && !bannerIds.isEmpty()) {
            bannerService.deleteBanners(bannerIds);
        }
        return "redirect:/admin/config/banner";
    }
}
