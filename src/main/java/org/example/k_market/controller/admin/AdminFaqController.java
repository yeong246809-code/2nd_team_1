package org.example.k_market.controller.admin;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.FaqDTO;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.UsersRepository;
import org.example.k_market.service.cs.FaqService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/cs/faq")
public class AdminFaqController {

    private final FaqService faqService;
    private final UsersRepository usersRepository;

    // 목록
    @GetMapping("/list")
    public String list(@RequestParam(required = false) String type,
                       Model model) {

        model.addAttribute("faqs", faqService.findByType(type));
        model.addAttribute("type", type);

        return "admin/cs/faq/list";
    }

    // 작성 화면
    @GetMapping("/write")
    public String write(Model model) {

        model.addAttribute("faq", new FaqDTO());

        return "admin/cs/faq/write";
    }

    // 작성 처리
    @PostMapping("/write")
    public String write(FaqDTO dto, HttpSession session) {

        String sessUser = (String) session.getAttribute("sessUser");

        if (sessUser == null) {
            return "redirect:/member/login";
        }

        Users user = usersRepository.findById(sessUser)
                .orElseThrow(() -> new IllegalArgumentException("회원이 없습니다."));

        dto.setMemberNo(user.getMemberNo());

        faqService.save(dto.toEntity().toDTO());

        return "redirect:/admin/cs/faq/list";
    }

    // 수정 화면
    @GetMapping("/modify/{no}")
    public String modify(@PathVariable int no, Model model) {

        model.addAttribute("faq", faqService.findById(no));

        return "admin/cs/faq/modify";
    }

    // 수정 처리
    @PostMapping("/modify/{no}")
    public String modify(@PathVariable int no,
                         FaqDTO dto) {

        faqService.update(no, dto);

        return "redirect:/admin/cs/faq/list";
    }

    // 삭제
    @GetMapping("/delete/{no}")
    public String delete(@PathVariable int no) {

        faqService.delete(no);

        return "redirect:/admin/cs/faq/list";
    }

    @PostMapping("/deleteChecked")
    public String deleteChecked(@RequestParam(value = "nos", required = false) List<Integer> nos) {

        if (nos != null && !nos.isEmpty()) {
            faqService.deleteChecked(nos);
        }

        return "redirect:/admin/cs/faq/list";
    }
}