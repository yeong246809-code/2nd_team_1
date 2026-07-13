package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.BannerDTO;
import org.example.k_market.dto.PageResponseDTO;
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

    // 1. 배너 목록 조회 (페이징, 위치 탭, 정렬 적용)
    @GetMapping("/banner")
    public String bannerPage(
            @RequestParam(value = "position", defaultValue = "MAIN1") String position,
            @RequestParam(value = "sort", defaultValue = "idDesc") String sort,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {

        PageResponseDTO<BannerDTO> pageResponseDTO = bannerService.getBannerList(position, sort, page);

        model.addAttribute("bannerList", pageResponseDTO.getDtoList());
        model.addAttribute("pageData", pageResponseDTO);
        model.addAttribute("currentPosition", position); // 현재 탭 유지용
        model.addAttribute("currentSort", sort);         // 현재 정렬 조건 유지용

        return "admin/config/banner";
    }

    // 2. 배너 등록
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
        // 등록했던 배너의 위치 탭으로 이동
        return "redirect:/admin/config/banner?position=" + bannerDTO.getPosition();
    }

    // 3. 상태(활성/비활성) 변경
    @GetMapping("/banner/status")
    public String toggleStatus(
            @RequestParam("id") Long id,
            @RequestParam(value = "position", defaultValue = "MAIN1") String position,
            @RequestParam(value = "sort", defaultValue = "idDesc") String sort,
            @RequestParam(value = "page", defaultValue = "1") int page) {

        bannerService.toggleStatus(id);

        // 상태 변경 후 현재 보던 페이지/탭/정렬 상태 그대로 유지
        return String.format("redirect:/admin/config/banner?position=%s&sort=%s&page=%d", position, sort, page);
    }

    // 4. 선택 삭제
    @PostMapping("/banner/delete")
    public String deleteBanners(
            @RequestParam(value = "bannerIds", required = false) List<Long> bannerIds,
            @RequestParam(value = "position", defaultValue = "MAIN1") String position,
            @RequestParam(value = "sort", defaultValue = "idDesc") String sort) {

        if (bannerIds != null && !bannerIds.isEmpty()) {
            bannerService.deleteBanners(bannerIds);
        }
        return String.format("redirect:/admin/config/banner?position=%s&sort=%s", position, sort);
    }
}