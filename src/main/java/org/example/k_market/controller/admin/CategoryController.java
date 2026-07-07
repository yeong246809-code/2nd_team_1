package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/config")
public class CategoryController {

    @GetMapping("/category")
    public String categoryPage(Model model) {
        return "admin/config/category";
    }
}
