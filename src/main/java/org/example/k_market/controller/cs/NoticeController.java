package org.example.k_market.controller.cs;

import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.Notice;
import org.example.k_market.service.cs.NoticeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cs/notice")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/list")
    public String list(Model model) {

        model.addAttribute("notices", noticeService.findAll());

        return "cs/notice/list";
    }

    @GetMapping("/view/{no}")
    public String view(@PathVariable int no, Model model) {

        Notice notice = noticeService.findById(no);

        model.addAttribute("notice", notice);

        return "cs/notice/view";
    }
}