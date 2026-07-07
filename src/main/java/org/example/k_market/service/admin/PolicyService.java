package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.PolicyDTO;
import org.example.k_market.entity.Policy;
import org.example.k_market.repository.PolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;

    /**
     * 1. 모든 약관 조회 (Thymeleaf 처리를 위해 ID를 Key로 하는 Map 형태로 반환)
     */
    public Map<Long, PolicyDTO> getPolicyList() {
        List<Policy> policies = policyRepository.findAll();

        // 데이터가 잘 넘어오는지 확인 (콘솔로 확인하세요)
        System.out.println("★ 조회된 데이터 개수: " + policies.size());

        return policies.stream()
                .map(Policy::toDTO)
                // 키(ID)가 중복되거나 문제가 있을 경우를 대비하여 (k, v) -> v 처리 추가
                .collect(Collectors.toMap(
                        PolicyDTO::getId,
                        Function.identity(),
                        (existing, replacement) -> existing // 중복 발생 시 기존 것 유지
                ));
    }

    /**
     * 2. 특정 약관 내용 업데이트
     */
    @Transactional
    public void updatePolicy(PolicyDTO dto) {
        // DB에서 해당 ID의 약관을 꺼내옴
        Policy policy = policyRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 약관이 존재하지 않습니다. ID: " + dto.getId()));

        // 꺼내온 엔티티의 내용을 변경 (트랜잭션이 끝나면 JPA가 알아서 UPDATE 쿼리를 날림)
        policy.updateContent(dto.getContent());
    }
}