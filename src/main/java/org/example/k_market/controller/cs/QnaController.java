package org.example.k_market.controller.cs;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.Member;
import org.example.k_market.entity.Qna;
import org.example.k_market.service.cs.QnaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cs/qna")
public class QnaController {

    private final QnaService qnaService;

    // 문의 목록
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("qnas", qnaService.findAll());
        return "cs/qna/list";
    }


    // 문의 상세
    @GetMapping("/view/{no}")
    public String view(@PathVariable int no, Model model) {

        Qna qna = qnaService.findById(no);
        Qna answer = qnaService.findAnswer(no);

        model.addAttribute("qna", qna);
        model.addAttribute("answer", answer);

        return "cs/qna/view";

    }


    // 문의 작성 화면
    @GetMapping("/write")
    public String write() {
        return "cs/qna/write";
    }



    // 문의 등록 처리
    @PostMapping("/write")
    public String write(Qna qna) {
        qnaService.save(qna);
        return "redirect:/cs/qna/list";
    }

    // 답변 내용 저장
    @PostMapping("/answer")
    public String answer(@RequestParam int parentNo,
                         @RequestParam String content){

        qnaService.saveAnswer(parentNo, content);

        return "redirect:/cs/qna/view/" + parentNo;
    }




}