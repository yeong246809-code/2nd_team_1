package org.example.k_market.controller.cs;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.QnaDTO;
import org.example.k_market.entity.Qna;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.UsersRepository;
import org.example.k_market.service.cs.QnaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cs/qna")
public class QnaController {

    private final QnaService qnaService;
    private final UsersRepository usersRepository;

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

    // 답변 내용 저장
    // admin 계정으로 로그인한 경우에만 답변 등록 가능
    @PostMapping("/answer")
    public String answer(@RequestParam int parentNo,
                         @RequestParam String content,
                         HttpSession session) {

        String sessUser = (String) session.getAttribute("sessUser");

        // 로그인하지 않은 경우
        if (sessUser == null) {
            return "redirect:/member/login";
        }

        // admin이 아닌 경우 답변 등록 차단
        if (!"admin".equals(sessUser)) {
            return "redirect:/cs/qna/view/" + parentNo;
        }

        qnaService.saveAnswer(parentNo, content);

        return "redirect:/cs/qna/view/" + parentNo;
    }

    // 문의 작성 화면
    // 상품 상세페이지의 "문의하기"에서 넘어올 때 productNo를 받아 write 폼에 hidden 값으로 채워줌
    @GetMapping("/write")
    public String write(@RequestParam(required = false) Long productNo,
                        Model model,
                        HttpSession session) {

        // 현재 프로젝트에서는 sessUser에 UsersDTO가 아니라 로그인 id 문자열이 저장되어 있음
        String sessUser = (String) session.getAttribute("sessUser");

        // 로그인하지 않은 경우 문의 작성 불가
        if (sessUser == null) {
            return "redirect:/member/login";
        }

        model.addAttribute("productNo", productNo); // null이면 일반 1:1문의, 값 있으면 상품문의
        return "cs/qna/write";
    }

    // 문의글 등록 처리
    @PostMapping("/write")
    public String write(QnaDTO qnaDTO, HttpSession session) {

        // 세션에서 로그인한 사용자 id 가져오기
        String sessUser = (String) session.getAttribute("sessUser");

        // 로그인하지 않은 경우 문의 등록 불가
        if (sessUser == null) {
            return "redirect:/member/login";
        }

        // id로 Users 엔티티 조회
        Users user = usersRepository.findById(sessUser)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 사용자가 보낸 값이 아니라 서버에서 찾은 memberNo를 저장
        qnaDTO.setMemberNo(user.getMemberNo());

        qnaService.save(qnaDTO);

        // 상품문의였으면 해당 상품 상세페이지로, 아니면 기존처럼 목록으로
        if (qnaDTO.getProductNo() != null) {
            return "redirect:/product/view?prodNo=" + qnaDTO.getProductNo();
        }
        return "redirect:/cs/qna/list";
    }
}