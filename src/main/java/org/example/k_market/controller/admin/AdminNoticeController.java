package org.example.k_market.controller.admin;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.NoticeDTO;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.UsersRepository;
import org.example.k_market.service.cs.NoticeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/cs/notice")

public class AdminNoticeController {

    private final NoticeService noticeService;
    private final UsersRepository usersRepository;

    // 목록
    @GetMapping("/list")
    public String list(Model model){

        model.addAttribute("notices", noticeService.findAll());

        return "admin/cs/notice/list";
    }

    // 상세
    @GetMapping("/view/{no}")
    public String view(@PathVariable int no, Model model){

        model.addAttribute("notice", noticeService.findById(no));

        return "admin/cs/notice/view";
    }

    // 작성 화면
    @GetMapping("/write")
    public String write(Model model) {

        model.addAttribute("notice", new NoticeDTO());

        return "admin/cs/notice/write";
    }

    // 작성
    @PostMapping("/write")
    public String write(NoticeDTO dto, HttpSession session) {

        String sessUser = (String) session.getAttribute("sessUser");

        if (sessUser == null) {
            return "redirect:/member/login";
        }

        Users user = usersRepository.findById(sessUser)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        dto.setMemberNo(user.getMemberNo());
        dto.setViewCount(0);

        noticeService.save(dto.toEntity());

        return "redirect:/admin/cs/notice/list";
    }

    // 수정 화면
    @GetMapping("/modify/{no}")
    public String modify(@PathVariable int no, Model model){

        model.addAttribute("notice", noticeService.findById(no));

        return "admin/cs/notice/modify";
    }

    // 수정
    @PostMapping("/modify/{no}")
    public String modify(@PathVariable int no,
                         NoticeDTO dto){

        noticeService.update(no, dto);

        return "redirect:/admin/cs/notice/view/" + no;
    }

    // 삭제
    @GetMapping("/delete/{no}")
    public String delete(@PathVariable int no){

        noticeService.delete(no);

        return "redirect:/admin/cs/notice/list";
    }

    @PostMapping("/deleteChecked")
    public String deleteChecked(@RequestParam(value = "nos", required = false) List<Integer> nos) {

        if (nos != null && !nos.isEmpty()) {
            noticeService.deleteChecked(nos);
        }

        return "redirect:/admin/cs/notice/list";
    }
}
