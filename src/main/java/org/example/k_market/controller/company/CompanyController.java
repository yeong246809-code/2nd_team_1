package org.example.k_market.controller.company;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.dao.CmMediaDAO;
import org.example.k_market.dao.CmStoryDAO;
import org.example.k_market.dto.CmRecruitDTO;
import org.example.k_market.entity.CmRecruit;
import org.example.k_market.repository.CmRecruitRepository;
import org.example.k_market.security.MyUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RequiredArgsConstructor
@Controller
@RequestMapping("/company")
public class CompanyController {

    private final CmStoryDAO cmStoryDAO;
    private final CmMediaDAO cmMediaDAO;
    private final CmRecruitRepository cmRecruitRepository;

    @GetMapping({"/index", "/main"})
    public String main() {
        return "company/main";
    }

    @GetMapping("/culture")
    public String culture() {
        return "company/culture";
    }

    @GetMapping("/story")
    public String story(Model model) {
        model.addAttribute("stories", cmStoryDAO.findAll());
        return "company/story";
    }

    // ===== 채용 목록 =====
    @GetMapping("/recruit")
    public String recruit(@AuthenticationPrincipal MyUserDetails userDetails, Model model) {
        model.addAttribute("recruits", cmRecruitRepository.findAllByOrderByIdDesc()); // 최신글이 위로
        model.addAttribute("isAdmin", isAdmin(userDetails));
        return "company/recruit";
    }

    // ===== 글쓰기 폼 =====
    @GetMapping("/recruit/write")
    public String recruitWriteForm(@AuthenticationPrincipal MyUserDetails userDetails, Model model) {
        if (!isAdmin(userDetails)) {
            return "redirect:/company/recruit";
        }
        model.addAttribute("recruit", new CmRecruitDTO());
        return "company/recruit-form";
    }

    // ===== 등록 처리 =====
    @PostMapping("/recruit/write")
    public String recruitWrite(@ModelAttribute CmRecruitDTO recruitDTO,
                               @AuthenticationPrincipal MyUserDetails userDetails) {
        if (!isAdmin(userDetails)) {
            return "redirect:/company/recruit";
        }
        cmRecruitRepository.save(recruitDTO.toEntity());
        return "redirect:/company/recruit";
    }

    // ===== 수정 폼 (같은 recruit-form.html 재사용) =====
    @GetMapping("/recruit/edit")
    public String recruitEditForm(@RequestParam int id,
                                  @AuthenticationPrincipal MyUserDetails userDetails, Model model) {
        if (!isAdmin(userDetails)) {
            return "redirect:/company/recruit";
        }
        CmRecruit recruit = cmRecruitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공고가 존재하지 않습니다: " + id));
        model.addAttribute("recruit", recruit.toDTO());
        return "company/recruit-form";
    }

    // ===== 수정 처리 =====
    @PostMapping("/recruit/edit")
    public String recruitEdit(@ModelAttribute CmRecruitDTO recruitDTO,
                              @AuthenticationPrincipal MyUserDetails userDetails) {
        if (!isAdmin(userDetails)) {
            return "redirect:/company/recruit";
        }
        cmRecruitRepository.save(recruitDTO.toEntity());
        return "redirect:/company/recruit";
    }

    // ===== 삭제 =====
    @PostMapping("/recruit/delete")
    public String recruitDelete(@RequestParam int id,
                                @AuthenticationPrincipal MyUserDetails userDetails) {
        if (!isAdmin(userDetails)) {
            return "redirect:/company/recruit";
        }
        cmRecruitRepository.deleteById(id);
        return "redirect:/company/recruit";
    }

    @GetMapping("/media")
    public String media(Model model) {
        model.addAttribute("medias", cmMediaDAO.findAll());
        return "company/media";
    }

    private boolean isAdmin(MyUserDetails userDetails) {
        return userDetails != null && "ADMIN".equals(userDetails.getUser().getRole());
    }
}