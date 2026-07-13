package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.PageResponseDTO;
import org.example.k_market.dto.ShopDTO;
import org.example.k_market.service.admin.ShopService;
import org.example.k_market.service.member.UsersService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/admin/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final UsersService usersService;

    // 1. 상점 목록 렌더링 (페이징 + 정렬 + 상태 필터 + 검색)
    @GetMapping("/list")
    public String shopList(@RequestParam(required = false, defaultValue = "name") String searchType,
                           @RequestParam(required = false, defaultValue = "") String keyword,
                           @RequestParam(required = false, defaultValue = "") String statusFilter,
                           @RequestParam(required = false, defaultValue = "default") String sort,
                           @RequestParam(required = false, defaultValue = "1") int page,
                           Model model) {

        PageResponseDTO<ShopDTO> pageResponseDTO = shopService.getShopList(searchType, keyword, statusFilter, sort, page);

        model.addAttribute("shopList", pageResponseDTO.getDtoList());
        model.addAttribute("pageData", pageResponseDTO);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("sort", sort);

        return "admin/shop/list";
    }

    // 2. 상점 상태 변경 (변경 후 기존 필터/페이징 유지)
    @PostMapping("/status")
    public String updateStatus(@RequestParam("memberNo") Integer memberNo,
                               @RequestParam("status") String status,
                               @RequestParam(required = false, defaultValue = "") String searchType,
                               @RequestParam(required = false, defaultValue = "") String keyword,
                               @RequestParam(required = false, defaultValue = "") String statusFilter,
                               @RequestParam(required = false, defaultValue = "default") String sort,
                               @RequestParam(required = false, defaultValue = "1") int page) {

        shopService.updateShopStatus(memberNo, status);
        String encodedKeyword = URLEncoder.encode(keyword != null ? keyword : "", StandardCharsets.UTF_8);

        return String.format("redirect:/admin/shop/list?searchType=%s&keyword=%s&statusFilter=%s&sort=%s&page=%d",
                searchType, encodedKeyword, statusFilter, sort, page);
    }

    // 3. 상점 선택 삭제 (삭제 후 기존 필터 유지)
    @PostMapping("/delete")
    public String delete(@RequestParam(required = false) List<Integer> memberNos,
                         @RequestParam(required = false, defaultValue = "") String searchType,
                         @RequestParam(required = false, defaultValue = "") String keyword,
                         @RequestParam(required = false, defaultValue = "") String statusFilter,
                         @RequestParam(required = false, defaultValue = "default") String sort,
                         RedirectAttributes redirectAttributes) {
        if (memberNos != null && !memberNos.isEmpty()) {
            shopService.deleteShops(memberNos);
            redirectAttributes.addFlashAttribute("message", "선택한 상점이 삭제(비활성)되었습니다.");
        }
        String encodedKeyword = URLEncoder.encode(keyword != null ? keyword : "", StandardCharsets.UTF_8);

        return String.format("redirect:/admin/shop/list?searchType=%s&keyword=%s&statusFilter=%s&sort=%s",
                searchType, encodedKeyword, statusFilter, sort);
    }

    // 4. 상점 등록
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

        usersService.registerSeller(id, password, passwordConfirm, company, representative, licenseNumber, reportNumber, phone, fax, "", "", "");
        return "redirect:/admin/shop/list";
    }
}