package org.example.k_market.controller.admin;

import org.example.k_market.dto.SalesStatusDTO;
import org.example.k_market.service.admin.AdminShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
public class AdminShopController {

    private final AdminShopService adminShopService;

    @GetMapping("/admin/shop/sales")
    public String getSalesStatus(
            @RequestParam(value = "periodType", defaultValue = "daily") String periodType,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<SalesStatusDTO> salesPage = adminShopService.getSalesStatusList(periodType, pageable);

        // HTML 대시보드 화면 표에 바인딩할 데이터 세팅
        model.addAttribute("salesList", salesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", salesPage.getTotalPages());
        model.addAttribute("currentPeriod", periodType);

        return "admin/shop/sales";
    }
    // 기존 AdminShopController 클래스 내부에 추가해주세요.

    @GetMapping("/admin/shop/list")
    public String getShopList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        // 한 페이지에 상점 10개씩 노출되도록 페이징 설정
        Pageable pageable = PageRequest.of(page, 10);

        // 서비스에서 상점 목록 데이터를 페이징 처리해서 가져옴 (서비스단 구현 필요)
        // Page<ShopDTO> shopPage = adminShopService.getShopList(pageable);

        // model.addAttribute("shopList", shopPage.getContent());
        // model.addAttribute("currentPage", page);
        // model.addAttribute("totalPages", shopPage.getTotalPages());

        return "admin/shop/list"; // 호출할 HTML 파일명 (admin/shop/list.html)
    }

}