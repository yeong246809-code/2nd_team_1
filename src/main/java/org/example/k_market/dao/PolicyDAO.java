package org.example.k_market.dao;

import org.example.k_market.dto.PolicyDTO;
import org.example.k_market.entity.Policy;
import org.example.k_market.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class PolicyDAO {

    private final PolicyRepository policyRepository;

    public PolicyDTO save(PolicyDTO dto) {
        // dto가 null이면 NullPointerException 발생 가능: Service에서 null 체크 후 호출하는 것을 권장
        Policy entity = dto.toEntity();
        Policy savedEntity = policyRepository.save(entity);
        return savedEntity.toDTO();
    }

    public Optional<PolicyDTO> findById(Long id) {
        // PK 타입이 엔티티의 @Id 타입과 다르면 컴파일 오류 발생
        return policyRepository.findById(id)
                .map(Policy::toDTO);
    }

    public List<PolicyDTO> findAll() {
        return policyRepository.findAll()
                .stream()
                // Entity 내부 연관관계가 LAZY이면 toDTO() 호출 시점에 LazyInitializationException 가능
                .map(Policy::toDTO)
                .toList();
    }

    public void deleteById(Long id) {
        // 존재하지 않는 PK 삭제를 막기 위해 existsById로 먼저 확인
        if (policyRepository.existsById(id)) {
            policyRepository.deleteById(id);
        }
    }
}
