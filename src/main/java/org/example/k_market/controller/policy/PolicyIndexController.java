package org.example.k_market.controller.policy;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.dto.PolicyDTO;
import org.example.k_market.service.admin.PolicyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@Log4j2
@RequiredArgsConstructor
public class PolicyIndexController {

    private final PolicyService policyService;

    @GetMapping("/policy/buyer")
    public String buyer(
            @RequestParam(required = false, defaultValue = PolicyService.BUYER_POLICY) String type,
            Model model) {
        List<PolicyDTO> policies = policyService.getOrderedPolicies();
        PolicyDTO selectedPolicy = policyService.getPolicy(type);

        model.addAttribute("policies", policies);
        model.addAttribute("selectedPolicy", selectedPolicy);
        model.addAttribute("selectedPolicyType", selectedPolicy.getPolicyType());
        return "policy/buyer";
    }
}