package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.PolicyDTO;
import org.example.k_market.entity.Policy;
import org.example.k_market.repository.PolicyRepository;
import org.example.k_market.service.admin.PolicyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/config")
public class PolicyController {

    private final PolicyService policyService;
    private final PolicyRepository policyRepository;

    // 1. 약관 관리 페이지 렌더링
    @GetMapping("/policy")
    public String policyPage(Model model) {
        model.addAttribute("policyList", policyRepository.findAll()); // List 자체를 넘김
        return "admin/config/policy";
    }

    // 2. 약관 내용 수정
    @PostMapping("/policy/update")
    public String updatePolicy(PolicyDTO policyDTO) {
        policyService.updatePolicy(policyDTO);

        // 처리가 끝나면 다시 약관 관리 페이지로 이동 (새로고침 효과)
        return "redirect:/admin/config/policy";
    }
}