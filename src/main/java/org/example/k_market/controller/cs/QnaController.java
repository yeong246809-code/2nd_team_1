package org.example.k_market.controller.cs;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.QnaDTO;
import org.example.k_market.dto.UsersDTO;
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


    // 답변 내용 저장
    @PostMapping("/answer")
    public String answer(@RequestParam int parentNo,
                         @RequestParam String content){

        qnaService.saveAnswer(parentNo, content);

        return "redirect:/cs/qna/view/" + parentNo;
    }


    // 글쓰기
    @GetMapping("/write")
    public String write(HttpSession session) {

        // 세션 확인용 로그
        System.out.println("sessUser = " + session.getAttribute("sessUser"));
        System.out.println("sessMember = " + session.getAttribute("sessMember"));
        System.out.println("member = " + session.getAttribute("member"));
        System.out.println("user = " + session.getAttribute("user"));

        UsersDTO sessUser = (UsersDTO) session.getAttribute("sessUser");

        if (sessUser == null) {
            return "redirect:/member/login";
        }

        return "cs/qna/write";
    }


    // 문의글 등록 처리
// write.html form에서 넘어온 title, content, type1, type2를 QnaDTO로 받는다.
// 작성자 번호(memberNo)는 사용자가 보내는 값이 아니라 로그인 세션에서 가져온다.
    @PostMapping("/write")
    public String write(QnaDTO qnaDTO, HttpSession session) {

        // 세션에서 로그인한 사용자 정보 가져오기
        UsersDTO sessUser = (UsersDTO) session.getAttribute("sessUser");

        // 로그인하지 않은 경우 글 등록 불가
        if (sessUser == null) {
            return "redirect:/member/login";
        }

        // qna 테이블에는 Users.id가 아니라 memberNo를 저장한다.
        qnaDTO.setMemberNo(sessUser.getMemberNo());

        // JPA 방식: Service → Repository.save()
        qnaService.save(qnaDTO);

        // 등록 후 문의 목록으로 이동
        return "redirect:/cs/qna/list";
    }


}