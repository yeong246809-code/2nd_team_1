package org.example.k_market.dao;

import org.example.k_market.dto.CouponDetailsDTO;
import org.example.k_market.entity.CouponDetails;
import org.example.k_market.repository.CouponDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class CouponDetailsDAO {

    private final CouponDetailsRepository couponDetailsRepository;

    public CouponDetailsDTO save(CouponDetailsDTO dto) {
        // dto가 null이면 NullPointerException 발생 가능: Service에서 null 체크 후 호출하는 것을 권장
        CouponDetails entity = dto.toEntity();
        CouponDetails savedEntity = couponDetailsRepository.save(entity);
        return savedEntity.toDTO();
    }

    public Optional<CouponDetailsDTO> findById(Long cuponDetailNo) {
        // PK 타입이 엔티티의 @Id 타입과 다르면 컴파일 오류 발생
        return couponDetailsRepository.findById(cuponDetailNo)
                .map(CouponDetails::toDTO);
    }

    public List<CouponDetailsDTO> findAll() {
        return couponDetailsRepository.findAll()
                .stream()
                // Entity 내부 연관관계가 LAZY이면 toDTO() 호출 시점에 LazyInitializationException 가능
                .map(CouponDetails::toDTO)
                .toList();
    }

    public void deleteById(Long cuponDetailNo) {
        // 존재하지 않는 PK 삭제를 막기 위해 existsById로 먼저 확인
        if (couponDetailsRepository.existsById(cuponDetailNo)) {
            couponDetailsRepository.deleteById(cuponDetailNo);
        }
    }
}
