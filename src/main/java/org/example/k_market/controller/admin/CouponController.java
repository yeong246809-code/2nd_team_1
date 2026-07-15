package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.*;
import org.example.k_market.entity.Product;
import org.example.k_market.entity.Shop;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.ShopRepository;
import org.example.k_market.repository.UsersRepository;
import org.example.k_market.service.admin.CouponService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final UsersRepository usersRepository;
    private final ShopRepository shopRepository;

    /**
     * 쿠폰 목록 조회 화면
     */
    @GetMapping("/list")
    public String list(PageRequestDTO pageRequestDTO, Model model,
                       @AuthenticationPrincipal UserDetails userDetails) {

        // 1. 목록 및 페이지네이션 데이터 조회
        PageResponseDTO<CouponDTO> responseDTO = couponService.getCouponList(pageRequestDTO);

        // 🚨 중요: HTML 및 _pagination 프래그먼트에서 사용할 변수명으로 매핑!
        model.addAttribute("pageData", responseDTO);
        model.addAttribute("pageRequestDTO", pageRequestDTO);

        // 2. 판매자로 로그인한 경우 등록 팝업에서 사용할 본인의 상품 목록 조회
        if (userDetails != null) {
            // 1. 기존 상품 목록 조회
            List<Product> sellerProducts = couponService.getSellerProducts(userDetails.getUsername());
            model.addAttribute("sellerProducts", sellerProducts);

            // 2. 🟢 상점명을 찾아 모델에 추가 (판매자일 경우)
            Users user = usersRepository.findById(userDetails.getUsername()).orElse(null);
            if (user != null && "SELLER".equalsIgnoreCase(user.getRole())) {
                Shop shop = shopRepository.findById(user.getMemberNo()).orElse(null);
                if(shop != null) model.addAttribute("myShopName", shop.getName());
            }
        }

        return "admin/coupon/list";
    }

    /**
     * 쿠폰 등록 처리
     */
    @PostMapping("/register")
    public String register(CouponDTO couponDTO,
                           @AuthenticationPrincipal UserDetails userDetails,
                           org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            if (userDetails == null) throw new IllegalArgumentException("로그인이 필요합니다.");
            couponService.registerCoupon(couponDTO, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("couponMessage", "쿠폰이 등록되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("couponError", e.getMessage());
        }
        return "redirect:/admin/coupon/list";
    }

    /**
     * 쿠폰 발급 종료 (JS Fetch AJAX 통신용)
     */
    @PostMapping("/stop")
    @ResponseBody
    public String stopCoupon(@RequestParam("couponNo") Long couponNo) {
        try {
            couponService.stopCoupon(couponNo);
            return "success";
        } catch (Exception e) {
            return "fail";
        }
    }

    @GetMapping("/issued")
    public String issuedList(PageRequestDTO pageRequestDTO, Model model) {
        // 기본 검색 타입을 '발급번호'로 세팅
        if(pageRequestDTO.getSearchType() == null) pageRequestDTO.setSearchType("couponDetailNo");

        PageResponseDTO<CouponDetailsDTO> responseDTO = couponService.getIssuedCouponList(pageRequestDTO);

        model.addAttribute("pageData", responseDTO);
        model.addAttribute("pageRequestDTO", pageRequestDTO);

        return "admin/coupon/issued";
    }

    /**
     * 쿠폰 발급 중단 (개별)
     */
    @PostMapping("/issued/stop")
    @ResponseBody
    public String stopIssuedCoupon(@RequestParam("couponDetailNo") Long couponDetailNo) {
        try {
            couponService.stopIssuedCoupon(couponDetailNo);
            return "success";
        } catch (Exception e) {
            return "fail";
        }
    }
}
