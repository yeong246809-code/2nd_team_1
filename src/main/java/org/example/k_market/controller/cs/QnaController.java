package org.example.k_market.controller.cs;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.QnaDTO;
import org.example.k_market.entity.Qna;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.UsersRepository;
import org.example.k_market.service.cs.QnaService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cs/qna")
public class QnaController {

    private static final int PAGE_SIZE = 5;
    private static final int PAGE_BLOCK_SIZE = 5;

    private final QnaService qnaService;
    private final UsersRepository usersRepository;

    // 문의 목록
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "1") int pg,
                       Model model) {

        int requestedPage = Math.max(pg, 1);
        Page<Qna> qnaPage = qnaService.findAll(requestedPage, PAGE_SIZE);

        // 마지막 페이지를 넘어선 주소로 접근하면 마지막 페이지로 보정
        if (qnaPage.getTotalPages() > 0 && requestedPage > qnaPage.getTotalPages()) {
            requestedPage = qnaPage.getTotalPages();
            qnaPage = qnaService.findAll(requestedPage, PAGE_SIZE);
        }

        int totalPages = qnaPage.getTotalPages();
        int startPage = totalPages == 0
                ? 0
                : ((requestedPage - 1) / PAGE_BLOCK_SIZE) * PAGE_BLOCK_SIZE + 1;
        int endPage = totalPages == 0
                ? 0
                : Math.min(startPage + PAGE_BLOCK_SIZE - 1, totalPages);

        model.addAttribute("qnas", qnaPage.getContent());
        model.addAttribute("qnaPage", qnaPage);
        model.addAttribute("pg", requestedPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

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

    // 답변 내용 저장
    @PostMapping("/answer")
    public String answer(@RequestParam int parentNo,
                         @RequestParam String content,
                         HttpSession session) {

        String sessUser = (String) session.getAttribute("sessUser");

        if (sessUser == null) {
            return "redirect:/member/login";
        }

        if (!"admin".equals(sessUser)) {
            return "redirect:/cs/qna/view/" + parentNo;
        }

        qnaService.saveAnswer(parentNo, content);

        return "redirect:/cs/qna/view/" + parentNo;
    }

    // 문의 작성 화면
    @GetMapping("/write")
    public String write(@RequestParam(required = false) Long prodNo,
                        Model model,
                        HttpSession session) {

        String sessUser = (String) session.getAttribute("sessUser");

        if (sessUser == null) {
            return "redirect:/member/login";
        }

        model.addAttribute("prodNo", prodNo);
        return "cs/qna/write";
    }

    // 문의글 등록 처리
    @PostMapping("/write")
    public String write(QnaDTO qnaDTO, HttpSession session) {

        String sessUser = (String) session.getAttribute("sessUser");

        if (sessUser == null) {
            return "redirect:/member/login";
        }

        Users user = usersRepository.findById(sessUser)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        qnaDTO.setMemberNo(user.getMemberNo());
        qnaService.save(qnaDTO);

        if (qnaDTO.getProdNo() != null) {
            return "redirect:/product/view?prodNo=" + qnaDTO.getProdNo();
        }

        return "redirect:/cs/qna/list";
    }
}
