package org.example.k_market.controller.admin;

import org.example.k_market.dto.CouponDTO;
import org.example.k_market.entity.Coupon;
import org.example.k_market.service.admin.AdminCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/admin/coupon")
public class AdminCouponController {

    @Autowired
    private AdminCouponService adminCouponService;

    // 1. 쿠폰 목록 페이지
    @GetMapping("/list")
    public String list(Model model, Principal principal,
                       @PageableDefault(size = 10, sort = "couponNo", direction = Sort.Direction.DESC) Pageable pageable) {

        // 1. 로그인 유저 정보 전달
        model.addAttribute("loggedInUserName", principal != null ? principal.getName() : "Guest");

        // 2. 서비스 호출은 딱 한 번만 수행
        Page<CouponDTO> couponPage = adminCouponService.getCouponList(pageable);

        // 3. 모델에 전달 (null 방지용)
        model.addAttribute("couponPage", couponPage != null ? couponPage : Page.empty());

        return "admin/coupon/list";
    }

    // 2. 쿠폰 상세 정보 조회 (팝업용)
    @GetMapping("/detail/{couponNo}")
    @ResponseBody
    public ResponseEntity<Coupon> getDetail(@PathVariable String couponNo) {
        return ResponseEntity.ok(adminCouponService.getCouponDetail(couponNo));
    }

    // 3. 쿠폰 등록 (관리자 전용)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody CouponDTO dto, Principal principal) {
        // 로그인한 유저를 발급자로 자동 설정
        dto.setIssuerName(principal.getName());

        adminCouponService.registerCoupon(dto);
        return ResponseEntity.ok().build();
    }

    // 4. 쿠폰 종료 (발급중 -> 종료)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/terminate/{couponNo}")
    @ResponseBody
    public ResponseEntity<?> terminate(@PathVariable String couponNo) {
        adminCouponService.terminateCoupon(couponNo);
        return ResponseEntity.ok().build();
    }
}