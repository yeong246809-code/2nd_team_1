package org.example.k_market.controller.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.dto.PolicyDTO;
import org.example.k_market.service.policy.PolicyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@Log4j2
@RequiredArgsConstructor
public class MemberIndexController {

    private final PolicyService policyService;

    @GetMapping("/member/join")
    public String join() {
        return "member/join";
    }

    @GetMapping("/member/register")
    public String register() {
        return "member/register";
    }

    @GetMapping("/member/registerseller")
    public String registerSeller() {
        return "member/registerseller";
    }

    @GetMapping("/member/signup")
    public String signup(@RequestParam(defaultValue = "user") String type, Model model) {
        boolean seller = "seller".equalsIgnoreCase(type);
        Map<String, PolicyDTO> signupPolicies = policyService.getSignupPolicies(seller);

        model.addAttribute("memberType", seller ? "seller" : "user");
        model.addAttribute("memberTypeName", seller ? "판매회원" : "일반회원");
        model.addAttribute("nextUrl", seller ? "/member/registerseller?agreed=true" : "/member/register?agreed=true");
        model.addAttribute("mainPolicy", signupPolicies.get("main"));
        model.addAttribute("financePolicy", signupPolicies.get("finance"));
        model.addAttribute("privacyPolicy", signupPolicies.get("privacy"));
        model.addAttribute("locationPolicy", signupPolicies.get("location"));
        return "member/signup";
    }

    @GetMapping("/member/welcome")
    public String welcome(
            @RequestParam(required = false, defaultValue = "회원") String id,
            @RequestParam(defaultValue = "user") String type,
            Model model) {
        boolean seller = "seller".equalsIgnoreCase(type);
        model.addAttribute("joinedId", id);
        model.addAttribute("memberTypeName", seller ? "판매회원" : "일반회원");
        model.addAttribute("welcomeMessage", seller
                ? "K-market 판매회원 신청이 완료되었습니다."
                : "K-market의 회원이 되신 것을 축하드립니다!");
        return "member/welcome";
    }

    @GetMapping("/member/find/userid")
    public String findUserid() {
        return "member/find/userid";
    }

    @GetMapping("/member/find/resultid")
    public String findResultId() {
        return "member/find/resultid";
    }

    @GetMapping("/member/find/password")
    public String findPassword() {
        return "member/find/password";
    }

    @GetMapping("/member/find/changePassword")
    public String changePassword() {
        return "member/find/changePassword";
    }
}