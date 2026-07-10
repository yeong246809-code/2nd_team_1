package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.k_market.dto.AdminPointListDTO;
import org.example.k_market.service.admin.AdminPointService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/member/point")
public class AdminPointController {

    private final AdminPointService adminPointService;

    // 1. 포인트 목록 조회 (GET 처리)
    @GetMapping
    public String pointList(
            @RequestParam(required = false, defaultValue = "id") String searchType,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "pointNo", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        // 서비스에서 리스트 가져오기
        Page<AdminPointListDTO> pointPage = adminPointService.getPoints(searchType, keyword, pageable);

        // html에서 반복문 돌릴 때 사용할 이름(pointList)에 데이터 담기
        model.addAttribute("pointList", pointPage.getContent());
        model.addAttribute("page", pointPage);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);

        return "admin/member/point";
    }

    // 2. 선택 삭제 및 포인트 회수 처리
    @PostMapping("/delete")
    public String deletePoints(
            @RequestParam(value = "selectedPointNos", required = false) List<Long> selectedPointNos,
            RedirectAttributes redirectAttributes) {

        if (selectedPointNos == null || selectedPointNos.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "선택된 포인트 내역이 없습니다.");
            return "redirect:/admin/member/point";
        }

        try {
            adminPointService.deletePointHistories(selectedPointNos);
            redirectAttributes.addFlashAttribute("message", "선택한 포인트 내역이 삭제되었으며 회원 잔여 포인트에 반영되었습니다.");
        } catch (Exception e) {
            log.error("포인트 내역 삭제 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", "처리 중 오류가 발생했습니다.");
        }

        return "redirect:/admin/member/point";
    }
}