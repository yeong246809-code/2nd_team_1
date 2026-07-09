package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.Qna;
import org.example.k_market.service.cs.QnaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/cs/qna")
public class AdminQnaController {

    private final QnaService qnaService;

    // 문의 목록
    @GetMapping("/list")
    public String list(Model model) {

        model.addAttribute("qnas", qnaService.findAll());

        return "admin/cs/qna/list";
    }

    // 문의 상세 + 답변 조회
    @GetMapping("/view/{no}")
    public String view(@PathVariable int no, Model model) {

        Qna qna = qnaService.findById(no);
        Qna answer = qnaService.findAnswer(no);

        model.addAttribute("qna", qna);
        model.addAttribute("answer", answer);

        return "admin/cs/qna/view";
    }

    // 답변 등록
    @PostMapping("/answer/{no}")
    public String answer(@PathVariable int no,
                         @RequestParam String content) {


        qnaService.saveOrUpdateAnswer(no, content);

        return "redirect:/admin/cs/qna/view/" + no;
    }


    //답변 삭제
    @GetMapping("/answer/delete/{no}")
    public String deleteAnswer(@PathVariable int no) {

        qnaService.deleteAnswer(no);

        return "redirect:/admin/cs/qna/view/" + no;
    }

    @PostMapping("/deleteChecked")
    public String deleteChecked(@RequestParam(value = "nos", required = false) List<Integer> nos) {

        if (nos != null && !nos.isEmpty()) {
            qnaService.deleteChecked(nos);
        }

        return "redirect:/admin/cs/qna/list";
    }
}