package org.example.k_market.dao;

import org.example.k_market.dto.SnsAccountDTO;
import org.example.k_market.entity.SnsAccount;
import org.example.k_market.repository.SnsAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class SnsAccountDAO {

    private final SnsAccountRepository snsAccountRepository;

    public SnsAccountDTO save(SnsAccountDTO dto) {
        // dto가 null이면 NullPointerException 발생 가능: Service에서 null 체크 후 호출하는 것을 권장
        SnsAccount entity = dto.toEntity();
        SnsAccount savedEntity = snsAccountRepository.save(entity);
        return savedEntity.toDTO();
    }

    public Optional<SnsAccountDTO> findById(Integer memberNo) {
        // PK 타입이 엔티티의 @Id 타입과 다르면 컴파일 오류 발생
        return snsAccountRepository.findById(memberNo)
                .map(SnsAccount::toDTO);
    }

    public List<SnsAccountDTO> findAll() {
        return snsAccountRepository.findAll()
                .stream()
                // Entity 내부 연관관계가 LAZY이면 toDTO() 호출 시점에 LazyInitializationException 가능
                .map(SnsAccount::toDTO)
                .toList();
    }

    public void deleteById(Integer memberNo) {
        // 존재하지 않는 PK 삭제를 막기 위해 existsById로 먼저 확인
        if (snsAccountRepository.existsById(memberNo)) {
            snsAccountRepository.deleteById(memberNo);
        }
    }
}
