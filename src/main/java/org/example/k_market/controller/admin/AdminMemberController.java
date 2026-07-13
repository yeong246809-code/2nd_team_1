package org.example.k_market.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.k_market.dto.MemberDTO;
import org.example.k_market.dto.PageResponseDTO;
import org.example.k_market.service.admin.AdminMemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/member")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    // 1. 회원 목록 렌더링 (페이징 + 정렬 + 상태 필터 + 검색)
    @GetMapping("/list")
    public String memberList(
            @RequestParam(required = false, defaultValue = "id") String searchType,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String statusFilter,
            @RequestParam(required = false, defaultValue = "idDesc") String sort,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        PageResponseDTO<MemberDTO> pageData = adminMemberService.getMembers(searchType, keyword, statusFilter, sort, page);

        model.addAttribute("members", pageData.getDtoList());
        model.addAttribute("pageData", pageData);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("sort", sort);

        return "admin/member/list";
    }

    // 2. 회원 정보 수정 (모달창 폼 전송 후 기존 필터/페이징 유지)
    @PostMapping("/update")
    public String updateMember(@ModelAttribute MemberDTO memberDTO,
                               @RequestParam(required = false, defaultValue = "id") String searchType,
                               @RequestParam(required = false, defaultValue = "") String keyword,
                               @RequestParam(required = false, defaultValue = "") String statusFilter,
                               @RequestParam(required = false, defaultValue = "idDesc") String sort,
                               @RequestParam(required = false, defaultValue = "1") int page,
                               RedirectAttributes redirectAttributes) {
        try {
            adminMemberService.updateMember(memberDTO);
            redirectAttributes.addFlashAttribute("message", "회원 정보가 수정되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "수정 중 오류가 발생했습니다.");
        }
        String encodedKeyword = URLEncoder.encode(keyword != null ? keyword : "", StandardCharsets.UTF_8);
        return String.format("redirect:/admin/member/list?searchType=%s&keyword=%s&statusFilter=%s&sort=%s&page=%d",
                searchType, encodedKeyword, statusFilter, sort, page);
    }

    // 3. 상태 변경 (정상/중지/재개 후 기존 필터/페이징 유지)
    @GetMapping("/changeStatus")
    public String changeStatus(@RequestParam int memberNo, @RequestParam String status,
                               @RequestParam(required = false, defaultValue = "id") String searchType,
                               @RequestParam(required = false, defaultValue = "") String keyword,
                               @RequestParam(required = false, defaultValue = "") String statusFilter,
                               @RequestParam(required = false, defaultValue = "idDesc") String sort,
                               @RequestParam(required = false, defaultValue = "1") int page,
                               RedirectAttributes redirectAttributes) {
        adminMemberService.changeStatus(memberNo, status);
        redirectAttributes.addFlashAttribute("message", "상태가 변경되었습니다.");
        String encodedKeyword = URLEncoder.encode(keyword != null ? keyword : "", StandardCharsets.UTF_8);
        return String.format("redirect:/admin/member/list?searchType=%s&keyword=%s&statusFilter=%s&sort=%s&page=%d",
                searchType, encodedKeyword, statusFilter, sort, page);
    }

    // 4. 회원 탈퇴 (비활성 처리 후 기존 필터/페이징 유지)
    @GetMapping("/deactivate")
    public String deactivateMember(@RequestParam int memberNo,
                                   @RequestParam(required = false, defaultValue = "id") String searchType,
                                   @RequestParam(required = false, defaultValue = "") String keyword,
                                   @RequestParam(required = false, defaultValue = "") String statusFilter,
                                   @RequestParam(required = false, defaultValue = "idDesc") String sort,
                                   @RequestParam(required = false, defaultValue = "1") int page,
                                   RedirectAttributes redirectAttributes) {
        try {
            adminMemberService.deactivateMember(memberNo);
            redirectAttributes.addFlashAttribute("message", "해당 회원의 개인정보가 삭제(비활성) 처리되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "비활성 처리 중 오류가 발생했습니다.");
        }
        String encodedKeyword = URLEncoder.encode(keyword != null ? keyword : "", StandardCharsets.UTF_8);
        return String.format("redirect:/admin/member/list?searchType=%s&keyword=%s&statusFilter=%s&sort=%s&page=%d",
                searchType, encodedKeyword, statusFilter, sort, page);
    }

    // 5. 선택 수정 (등급 일괄 변경 후 기존 필터/페이징 유지)
    @PostMapping("/bulkUpdateGrade")
    public String bulkUpdateGrade(
            @RequestParam(value = "selectedMemberNos", required = false) List<Integer> selectedMemberNos,
            @RequestParam(required = false, defaultValue = "id") String searchType,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String statusFilter,
            @RequestParam(required = false, defaultValue = "idDesc") String sort,
            @RequestParam(required = false, defaultValue = "1") int page,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        if (selectedMemberNos == null || selectedMemberNos.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "선택된 회원이 없습니다.");
            String encodedKeyword = URLEncoder.encode(keyword != null ? keyword : "", StandardCharsets.UTF_8);
            return String.format("redirect:/admin/member/list?searchType=%s&keyword=%s&statusFilter=%s&sort=%s&page=%d",
                    searchType, encodedKeyword, statusFilter, sort, page);
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

        String encodedKeyword = URLEncoder.encode(keyword != null ? keyword : "", StandardCharsets.UTF_8);
        return String.format("redirect:/admin/member/list?searchType=%s&keyword=%s&statusFilter=%s&sort=%s&page=%d",
                searchType, encodedKeyword, statusFilter, sort, page);
    }
}