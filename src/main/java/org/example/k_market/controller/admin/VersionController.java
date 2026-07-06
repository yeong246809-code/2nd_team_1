package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.dto.VersionDTO;
import org.example.k_market.service.admin.VersionService;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Controller
public class VersionController {

    private final VersionService versionService;

    @GetMapping("/admin/config/version")
    public String list(Model model) {
        List<VersionDTO> dtoList = versionService.getAll();
        model.addAttribute("dtoList", dtoList);

        return "admin/config/version";
    }
}
