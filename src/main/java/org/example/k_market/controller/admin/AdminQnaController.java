package org.example.k_market.controller.admin;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.Product;
import org.example.k_market.entity.Qna;
import org.example.k_market.repository.UsersRepository;
import org.example.k_market.service.ProductService;
import org.example.k_market.service.cs.QnaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/cs/qna")
public class AdminQnaController {

    private final QnaService qnaService;
    private final UsersRepository usersRepository;
    private final ProductService productService;

    // 문의 리스트
    @GetMapping("/list")
    public String list(Model model, HttpSession session) {

        String sessUser = (String) session.getAttribute("sessUser");

        if (sessUser == null) {
            return "redirect:/member/login";
        }

        if (!"admin".equals(sessUser)) {
            return "redirect:/";
        }

        model.addAttribute("qnas", qnaService.findAll());

        return "admin/cs/qna/list";
    }

    // 관리자 문의 상세
    @GetMapping("/view/{no}")
    public String view(@PathVariable int no,
                       Model model,
                       HttpSession session) {

        String sessUser = (String) session.getAttribute("sessUser");

        if (sessUser == null) {
            return "redirect:/member/login";
        }

        if (!"admin".equals(sessUser)) {
            return "redirect:/";
        }

        Qna qna = qnaService.findById(no);
        Qna answer = qnaService.findAnswer(no);

        model.addAttribute("qna", qna);
        model.addAttribute("answer", answer);

        // 상품이 연결된 문의인 경우에만 상품 조회
        if (qna.getProdNo() != null) {

            Product product = productService.findById(qna.getProdNo());

            model.addAttribute("product", product);
        }

        return "admin/cs/qna/view";


    }

    // 답변 등록 및 수정
    @PostMapping("/answer/{no}")
    public String answer(@PathVariable int no,
                         @RequestParam String content,
                         HttpSession session) {

        String sessUser = (String) session.getAttribute("sessUser");

        if (sessUser == null) {
            return "redirect:/member/login";
        }

        if (!"admin".equals(sessUser)) {
            return "redirect:/admin/cs/qna/view/" + no;
        }

        qnaService.saveAnswer(no, content);

        return "redirect:/admin/cs/qna/view/" + no;
    }

    // 답변 삭제
    @GetMapping("/answer/delete/{no}")
    public String deleteAnswer(@PathVariable int no,
                               HttpSession session) {

        String sessUser = (String) session.getAttribute("sessUser");

        if (!"admin".equals(sessUser)) {
            return "redirect:/admin/cs/qna/view/" + no;
        }

        qnaService.deleteAnswer(no);

        return "redirect:/admin/cs/qna/view/" + no;
    }
}