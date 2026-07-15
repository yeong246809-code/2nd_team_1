package org.example.k_market.controller.cs;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.Notice;
import org.example.k_market.service.cs.NoticeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cs/notice")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/list")
    public String list(
            @RequestParam(defaultValue = "전체") String type,
            Model model) {

        model.addAttribute("notices", noticeService.findByType(type));
        model.addAttribute("selectedType", type);

        return "cs/notice/list";
    }

    @GetMapping("/view/{no}")
    public String view(@PathVariable int no, Model model) {

        Notice notice = noticeService.findById(no);

        model.addAttribute("notice", notice);

        return "cs/notice/view";
    }

    // 공지사항 작성 화면
    // GET /cs/notice/write
    @GetMapping("/write")
    public String write(HttpSession session) {

        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        // 관리자만 작성 화면 접근 가능
        if (!Boolean.TRUE.equals(isAdmin)) {
            return "redirect:/cs/notice/list";
        }

        return "cs/notice/write";
    }

    // 공지사항 등록 처리
    // POST /cs/notice/write
    @PostMapping("/write")
    public String register(Notice notice, HttpSession session) {

        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        // 관리자만 등록 가능
        if (!Boolean.TRUE.equals(isAdmin)) {
            return "redirect:/cs/notice/list";
        }

        noticeService.save(notice);

        return "redirect:/cs/notice/list";
    }
}