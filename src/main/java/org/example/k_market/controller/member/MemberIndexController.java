package org.example.k_market.controller.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
@RequiredArgsConstructor
public class MemberIndexController {

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
    public String signup() {
        return "member/signup";
    }

    @GetMapping("/member/welcome")
    public String welcome() {
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
