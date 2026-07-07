package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.PolicyDTO;
import org.example.k_market.entity.Policy;
import org.example.k_market.repository.PolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyService {

    public static final String BUYER_POLICY = "구매회원 약관";
    public static final String SELLER_POLICY = "판매회원약관";
    public static final String FINANCE_POLICY = "전자금융거래 약관";
    public static final String LOCATION_POLICY = "위치정보 이용약관";
    public static final String PRIVACY_POLICY = "개인정보처리방침";

    private static final List<String> DISPLAY_ORDER = List.of(
            BUYER_POLICY,
            SELLER_POLICY,
            FINANCE_POLICY,
            LOCATION_POLICY,
            PRIVACY_POLICY
    );

    private final PolicyRepository policyRepository;

    public Map<Long, PolicyDTO> getPolicyList() {
        return policyRepository.findAll()
                .stream()
                .map(Policy::toDTO)
                .collect(Collectors.toMap(
                        PolicyDTO::getId,
                        Function.identity(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    public Map<String, PolicyDTO> getSignupPolicies(boolean seller) {
        Map<String, PolicyDTO> policies = getPolicyMap();
        String mainPolicyType = seller ? SELLER_POLICY : BUYER_POLICY;

        Map<String, PolicyDTO> signupPolicies = new LinkedHashMap<>();
        signupPolicies.put("main", findOrFallback(policies, mainPolicyType));
        signupPolicies.put("finance", findOrFallback(policies, FINANCE_POLICY));
        signupPolicies.put("privacy", findOrFallback(policies, PRIVACY_POLICY));
        signupPolicies.put("location", findOrFallback(policies, LOCATION_POLICY));
        return signupPolicies;
    }

    public List<PolicyDTO> getOrderedPolicies() {
        Map<String, PolicyDTO> policies = getPolicyMap();
        return DISPLAY_ORDER.stream()
                .map(policyType -> findOrFallback(policies, policyType))
                .toList();
    }

    public PolicyDTO getPolicy(String policyType) {
        Map<String, PolicyDTO> policies = getPolicyMap();
        String targetType = (policyType == null || policyType.isBlank()) ? BUYER_POLICY : policyType;
        return findOrFallback(policies, targetType);
    }

    @Transactional
    public void updatePolicy(PolicyDTO dto) {
        Policy policy = policyRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 약관이 존재하지 않습니다. ID: " + dto.getId()));
        policy.updateContent(dto.getContent());
    }

    private Map<String, PolicyDTO> getPolicyMap() {
        return policyRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        Policy::getPolicyType,
                        Policy::toDTO,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
    }

    private PolicyDTO findOrFallback(Map<String, PolicyDTO> policies, String policyType) {
        PolicyDTO policy = policies.get(policyType);
        if (policy == null && SELLER_POLICY.equals(policyType)) {
            policy = policies.get("판매회원 약관");
        }
        if (policy != null) {
            return policy;
        }

        return PolicyDTO.builder()
                .policyType(policyType)
                .content("등록된 약관 내용이 없습니다. policy 테이블의 policyType과 content를 확인해주세요.")
                .build();
    }
}