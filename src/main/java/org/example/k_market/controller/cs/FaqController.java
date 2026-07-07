package org.example.k_market.controller.cs;

import lombok.RequiredArgsConstructor;
import org.example.k_market.service.cs.FaqService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cs/faq")
public class FaqController {

    private final FaqService faqService;

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "회원") String type1,
                       Model model) {

        model.addAttribute("type1", type1);
        model.addAttribute("faqGroups", faqService.findFaqGroupsByType1(type1));

        return "cs/faq/list";
    }
}