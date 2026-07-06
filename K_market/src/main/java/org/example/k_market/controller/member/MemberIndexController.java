package org.example.k_market.controller.member;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@Controller
@RequestMapping("/member") // 💡 주소창에 /member로 시작하는 요청들을 이 컨트롤러가 잡습니다.
public class MemberIndexController {

    // 1. 로그인 페이지 이동 (/member/login)
    @GetMapping("/login")
    public String login() {
        log.info("MemberController - 로그인 페이지 이동");

        // 💡 src/main/resources/templates/member/login.html 파일을 찾아갑니다.
        return "member/login";
    }

    // 2. 마이페이지 이동 (/member/mypage)
    @GetMapping("/mypage")
    public String myPage() {
        log.info("MemberController - 마이페이지 이동");

        // 💡 src/main/resources/templates/member/mypage.html 파일을 찾아갑니다.
        return "member/mypage";
    }

    // 3. 회원가입 페이지 이동 (/member/join)
    @GetMapping("/join")
    public String join() {
        log.info("MemberController - 회원가입 페이지 이동");
        return "member/join";
    }
}