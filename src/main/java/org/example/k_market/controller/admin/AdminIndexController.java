package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
@RequiredArgsConstructor
public class AdminIndexController {

    @GetMapping("/admin/index")
    public String adminIndex() {
        return "admin/index";
    }

}
    
