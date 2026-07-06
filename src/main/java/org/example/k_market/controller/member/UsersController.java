package org.example.k_market.controller.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Log4j2
@Controller
@RequiredArgsConstructor
public class UsersController {

    @GetMapping("/member/login")
    public String loginPage() {
        return "member/login";
    }

}