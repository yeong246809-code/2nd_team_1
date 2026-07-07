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

    @GetMapping("/cs/faq/list")
    public String faqList() {
        return "cs/faq/list";
    }

    @GetMapping("/cs/notice/list")
    public String noticeList() {
        return "cs/notice/list";
    }

    @GetMapping("/cs/notice/view")
    public String noticeView(Model model) {
        model.addAttribute("notice", Map.of(
                "category", "[안내]",
                "title", "K-market 공지사항입니다.",
                "regDate", LocalDateTime.now(),
                "hit", 124,
                "content", "<p>K-market 공지사항 상세 화면입니다.</p>"
        ));
        return "cs/notice/view";
    }

    @GetMapping("/cs/qna/list")
    public String qnaList() {
        return "cs/qna/list";
    }
}
