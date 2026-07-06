package org.example.k_market.controller;

import org.example.k_market.dto.SalesStatusDTO;
import org.example.k_market.service.AdminShopService;
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
            // 기본값을 'daily'(일별)로 설정하고, 요청이 들어오면 동적으로 바뀝니다.
            @RequestParam(value = "periodType", defaultValue = "daily") String periodType,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10);

        // 서비스 호출 시 periodType을 인자로 넘겨줍니다.
        Page<SalesStatusDTO> salesPage = adminShopService.getSalesStatusList(periodType, pageable);

        // HTML 템플릿에서 쓸 변수들 담기
        model.addAttribute("salesList", salesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", salesPage.getTotalPages());
        model.addAttribute("currentPeriod", periodType); // 현재 선택된 옵션 유지용

        return "admin/shop/sales"; // 본인의 html 경로에 맞게 지정
    }
}