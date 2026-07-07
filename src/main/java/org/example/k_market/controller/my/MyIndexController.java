package org.example.k_market.controller.my;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
@RequiredArgsConstructor
public class MyIndexController {


    @GetMapping("/my/index")
    public String index() {
        return "my/index";
    }
}
