package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.ShopDTO;
import org.example.k_market.service.admin.ShopService;
import org.example.k_market.service.member.UsersService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final UsersService usersService; // 상점 등록을 위해 주입 (기존 로직 활용)

    // 1. 상점 목록 렌더링 및 검색
    @GetMapping("/list")
    public String shopList(@RequestParam(required = false) String searchType,
                           @RequestParam(required = false) String keyword,
                           Model model) {

        List<ShopDTO> shopList = shopService.getShopList(searchType, keyword);

        model.addAttribute("shopList", shopList);
        model.addAttribute("searchType", searchType); // 검색 타입 유지
        model.addAttribute("keyword", keyword);       // 검색어 유지

        return "admin/shop/list"; // templates/admin/shop/list.html 반환
    }

    // 2. 상점 상태 변경 (중단 / 재개 / 승인)
    @PostMapping("/status")
    public String updateStatus(@RequestParam("memberNo") Integer memberNo,
                               @RequestParam("status") String status) {
        shopService.updateShopStatus(memberNo, status);
        return "redirect:/admin/shop/list";
    }

    // 3. 상점 일괄 삭제 (선택 삭제)
    @PostMapping("/delete")
    public String delete(@RequestParam List<Integer> memberNos, RedirectAttributes redirectAttributes) {
        shopService.deleteShops(memberNos);
        redirectAttributes.addFlashAttribute("message", "선택한 상점이 삭제(비활성)되었습니다.");
        return "redirect:/admin/shop/list";
    }

    // 4. 새로운 상점 등록 (모달창 폼 전송)
    @PostMapping("/register")
    public String registerShop(
            @RequestParam("id") String id,
            @RequestParam("password") String password,
            @RequestParam("passwordConfirm") String passwordConfirm,
            @RequestParam("company") String company,
            @RequestParam("representative") String representative,
            @RequestParam("licenseNumber") String licenseNumber,
            @RequestParam("reportNumber") String reportNumber,
            @RequestParam("phone") String phone,
            @RequestParam(value = "fax", required = false) String fax) {

        // 화면에 주소 필드가 없으므로 주소 관련 파라미터는 빈 문자열 처리하여 UsersService 에 전달
        usersService.registerSeller(
                id, password, passwordConfirm,
                company, representative, licenseNumber, reportNumber,
                phone, fax, "", "", ""
        );

        return "redirect:/admin/shop/list";
    }
}