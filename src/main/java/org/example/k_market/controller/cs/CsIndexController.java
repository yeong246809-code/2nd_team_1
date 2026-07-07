package org.example.k_market.controller.cs;

import lombok.RequiredArgsConstructor;
import org.example.k_market.service.cs.NoticeService;
import org.example.k_market.service.cs.QnaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class CsIndexController {

    private final NoticeService noticeService;
    private final QnaService qnaService;

    // 고객센터 메인
    @GetMapping({"/cs", "/cs/index"})
    public String index(Model model) {

        // 공지사항 최신글 5개
        model.addAttribute("notices", noticeService.findTop5());

        // 문의글 최신글 5개
        model.addAttribute("qnas", qnaService.findTop5());

        return "cs/index";
    }
}