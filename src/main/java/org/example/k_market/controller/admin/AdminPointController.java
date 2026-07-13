package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.k_market.dto.AdminPointListDTO;
import org.example.k_market.dto.PageResponseDTO;
import org.example.k_market.service.admin.AdminPointService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/member/point")
public class AdminPointController {

    private final AdminPointService adminPointService;

    @GetMapping
    public String pointList(
            @RequestParam(required = false, defaultValue = "id") String searchType,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "1") int page,
            Model model) {

        PageResponseDTO<AdminPointListDTO> pageData = adminPointService.getPoints(searchType, keyword, page);

        model.addAttribute("pointList", pageData.getDtoList());
        model.addAttribute("pageData", pageData);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);

        return "admin/member/point";
    }

    @PostMapping("/delete")
    public String deletePoints(
            @RequestParam(value = "selectedPointNos", required = false) List<Long> selectedPointNos,
            @RequestParam(required = false, defaultValue = "id") String searchType,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "1") int page,
            RedirectAttributes redirectAttributes) {

        if (selectedPointNos == null || selectedPointNos.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "선택된 포인트 내역이 없습니다.");
            String encodedKeyword = URLEncoder.encode(keyword != null ? keyword : "", StandardCharsets.UTF_8);
            return String.format("redirect:/admin/member/point?searchType=%s&keyword=%s&page=%d", searchType, encodedKeyword, page);
        }

        try {
            adminPointService.deletePointHistories(selectedPointNos);
            redirectAttributes.addFlashAttribute("message", "선택한 포인트 내역이 삭제되었으며 회원 잔여 포인트에 반영되었습니다.");
        } catch (Exception e) {
            log.error("포인트 내역 삭제 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", "처리 중 오류가 발생했습니다.");
        }

        String encodedKeyword = URLEncoder.encode(keyword != null ? keyword : "", StandardCharsets.UTF_8);
        return String.format("redirect:/admin/member/point?searchType=%s&keyword=%s&page=%d", searchType, encodedKeyword, page);
    }
}