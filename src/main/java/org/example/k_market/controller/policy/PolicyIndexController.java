package org.example.k_market.controller.policy;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
@RequiredArgsConstructor
public class PolicyIndexController {

    @GetMapping("/policy/buyer")
    public String buyer() {
        return "policy/buyer";
    }
}
