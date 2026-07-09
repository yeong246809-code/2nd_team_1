package org.example.k_market.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.k_market.dto.MemberDTO;
import org.example.k_market.service.admin.AdminMemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/member")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    // 1. 회원 목록 및 검색
    @GetMapping("/list")
    public String memberList(
            @RequestParam(required = false, defaultValue = "id") String searchType,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "memberNo", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        Page<MemberDTO> memberPage = adminMemberService.getMembers(searchType, keyword, pageable);

        model.addAttribute("members", memberPage.getContent());
        model.addAttribute("page", memberPage);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);

        return "admin/member/list";
    }

    // 2. 회원 정보 수정
    @PostMapping("/update")
    public String updateMember(@ModelAttribute MemberDTO memberDTO, RedirectAttributes redirectAttributes) {
        try {
            adminMemberService.updateMember(memberDTO);
            redirectAttributes.addFlashAttribute("message", "회원 정보가 수정되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "수정 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/member/list";
    }

    // 3. 상태 변경 (정상/중지/재개)
    @GetMapping("/changeStatus")
    public String changeStatus(@RequestParam int memberNo, @RequestParam String status, RedirectAttributes redirectAttributes) {
        adminMemberService.changeStatus(memberNo, status);
        redirectAttributes.addFlashAttribute("message", "상태가 변경되었습니다.");
        return "redirect:/admin/member/list";
    }

    // 4. 회원 탈퇴 (비활성)
    @GetMapping("/deactivate")
    public String deactivateMember(@RequestParam int memberNo, RedirectAttributes redirectAttributes) {
        try {
            adminMemberService.deactivateMember(memberNo);
            redirectAttributes.addFlashAttribute("message", "해당 회원의 개인정보가 삭제(비활성) 처리되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "비활성 처리 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/member/list";
    }

    // 5. 선택 수정 (등급 일괄 변경)
    @PostMapping("/bulkUpdateGrade")
    public String bulkUpdateGrade(
            @RequestParam(value = "selectedMemberNos", required = false) List<Integer> selectedMemberNos,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        if (selectedMemberNos == null || selectedMemberNos.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "선택된 회원이 없습니다.");
            return "redirect:/admin/member/list";
        }

        Map<Integer, Integer> gradeMap = new HashMap<>();
        for (Integer memberNo : selectedMemberNos) {
            String gradeStr = request.getParameter("grade_" + memberNo);
            if (gradeStr != null) {
                gradeMap.put(memberNo, Integer.parseInt(gradeStr));
            }
        }

        adminMemberService.bulkUpdateGrade(selectedMemberNos, gradeMap);
        redirectAttributes.addFlashAttribute("message", "등급이 일괄 수정되었습니다.");

        return "redirect:/admin/member/list";
    }
}