package org.example.k_market.controller;

import lombok.RequiredArgsConstructor;
import org.example.k_market.security.MyUserDetails;
import org.example.k_market.service.CouponIssuanceService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CouponClaimController {
    private final CouponIssuanceService couponIssuanceService;

    @GetMapping("/coupon/claim")
    public String claim(@RequestParam(required = false) Long couponNo,
                        @RequestParam(required = false) String couponName,
                        @AuthenticationPrincipal MyUserDetails userDetails,
                        RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/member/login";
        try {
            if (couponNo != null) couponIssuanceService.claimByCouponNo(couponNo, userDetails.getUser().getMemberNo());
            else if (couponName != null && !couponName.isBlank()) {
                couponIssuanceService.claimByCouponName(couponName.trim(), userDetails.getUser().getMemberNo());
            } else throw new IllegalArgumentException("발급할 쿠폰 정보가 없습니다.");
            redirectAttributes.addFlashAttribute("couponMessage", "쿠폰이 발급되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("couponError", e.getMessage());
        }
        return "redirect:/my/coupon";
    }
}
