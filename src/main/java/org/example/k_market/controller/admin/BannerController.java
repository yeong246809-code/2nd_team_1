package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.BannerDTO;
import org.example.k_market.service.admin.BannerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/config")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    // 배너 목록 조회
    @GetMapping("/banner")
    public String bannerPage(Model model) {
        List<BannerDTO> list = bannerService.getAllBanners();
        model.addAttribute("bannerList", list != null ? list : new ArrayList<>()); // null 방지
        return "admin/config/banner";
    }

    // 배너 등록
    @PostMapping("/banner/add")
    public String addBanner(BannerDTO bannerDTO, @RequestParam("file") MultipartFile file) throws IOException {
        bannerService.registerBanner(bannerDTO, file);
        return "redirect:/admin/config/banner";
    }

    // 상태 변경 (활성/비활성)
    @PostMapping("/banner/status")
    public String toggleStatus(@RequestParam("id") Long id) {
        bannerService.toggleStatus(id);
        return "redirect:/admin/config/banner";
    }
}