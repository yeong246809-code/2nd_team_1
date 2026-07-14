package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.CouponDTO;
import org.example.k_market.entity.Coupon;
import org.example.k_market.repository.CouponDetailsRepository;
import org.example.k_market.repository.CouponRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AdminCouponServiceImpl implements AdminCouponService {
    private final CouponRepository couponRepository;
    private final CouponDetailsRepository couponDetailsRepository;

    @Override
    public Page<CouponDTO> getCouponList(Pageable pageable) {
        Page<Coupon> couponPage = couponRepository.findAll(pageable);

        return couponPage.map(coupon -> {
            CouponDTO dto = coupon.toDTO();
            dto.setIssueCount(couponDetailsRepository.countByCouponNo(coupon.getCouponNo()));
            dto.setUseCount(couponDetailsRepository.countByCouponNoAndIsUsed(coupon.getCouponNo(), "Y"));
            return dto;
        });
    }

    @Override
    public Coupon getCouponDetail(String couponNo) {
        return couponRepository.findById(Long.valueOf(couponNo))
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
    }

    @Transactional
    @Override
    public void registerCoupon(CouponDTO dto) {

        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        Integer validDays = dto.getValidDays();

        // 혜택 타입 판별 로직 추가
        String benefitType = dto.getBenefitValue() > 0 ? "AMOUNT" : "FREE_SHIPPING";
        if (dto.getBenefitValue() == 0 && dto.getBenefitType() != null) {
            benefitType = dto.getBenefitType(); // % 할인인 경우
        }

        Coupon coupon = Coupon.builder()
                .issuerName(dto.getIssuerName())
                .couponType(dto.getCouponType())
                .name(dto.getName())
                .benefitType(benefitType)
                .benefitValue(dto.getBenefitValue())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .validDays(dto.getValidDays())
                .notes(dto.getNotes())
                .status("ACTIVE") // 신규 등록은 항상 ACTIVE
                .build();

        couponRepository.save(coupon);
    }

    @Transactional
    @Override
    public void terminateCoupon(String couponNo) {
        Coupon coupon = couponRepository.findById(Long.valueOf(couponNo))
                .orElseThrow(() -> new IllegalArgumentException("쿠폰 없음"));
        coupon.setStatus("ENDED"); // '종료' 대신 명확한 상수 사용 권장
    }
}
