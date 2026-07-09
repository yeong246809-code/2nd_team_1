package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.Qna;
import org.example.k_market.service.cs.QnaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/cs/qna")
public class AdminQnaController {

    private final QnaService qnaService;

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("qnas", qnaService.findAll());
        return "admin/cs/qna/list";
    }

    @GetMapping("/view/{no}")
    public String view(@PathVariable int no, Model model) {
        Qna qna = qnaService.findById(no);

        model.addAttribute("qna", qna);
        model.addAttribute("answer", qnaService.findAnswer(no));
        return "admin/cs/qna/view";
    }

    @PostMapping("/answer/{no}")
    public String saveAnswer(@PathVariable int no,
                             @RequestParam String answer) {
        qnaService.saveAnswer(no, answer);
        return "redirect:/admin/cs/qna/view/" + no;
    }

    @GetMapping("/answer/delete/{no}")
    public String deleteAnswer(@PathVariable int no) {
        qnaService.deleteAnswer(no);
        return "redirect:/admin/cs/qna/view/" + no;
    }
}
