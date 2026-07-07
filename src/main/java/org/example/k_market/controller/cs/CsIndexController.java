package org.example.k_market.controller.cs;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@Log4j2
@RequiredArgsConstructor
public class CsIndexController {

    @GetMapping("/cs/index")
    public String index() {
        return "cs/index";
    }
}