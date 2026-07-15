package org.example.k_market.service;

import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.Coupon;
import org.example.k_market.entity.CouponDetails;
import org.example.k_market.repository.CouponDetailsRepository;
import org.example.k_market.repository.CouponRepository;
import org.example.k_market.repository.MemberRepository;
import org.example.k_market.repository.UsersRepository;
import org.example.k_market.entity.Member;
import org.example.k_market.entity.Users;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CouponIssuanceService {
    public static final String INDIVIDUAL_PRODUCT = "개별상품 할인";
    public static final String ORDER_DISCOUNT = "주문상품 할인";
    public static final String FREE_SHIPPING = "배송비 무료";
    public static final String WELCOME_SHIPPING_COUPON = "가입축하 배송비쿠폰";
    public static final String BIRTHDAY_COUPON = "생일축하 쿠폰";

    private final CouponRepository couponRepository;
    private final CouponDetailsRepository couponDetailsRepository;
    private final MemberRepository memberRepository;
    private final UsersRepository usersRepository;

    @Transactional
    public void claimByCouponNo(long couponNo, int memberNo) {
        Coupon coupon = couponRepository.findByIdForUpdate(couponNo)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
        claim(coupon, memberNo);
    }

    @Transactional
    public void claimByCouponName(String couponName, int memberNo) {
        Coupon found = couponRepository.findFirstByNameAndStatusOrderByCouponNoDesc(couponName, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("발급 가능한 '" + couponName + "'을 찾을 수 없습니다."));
        Coupon coupon = couponRepository.findByIdForUpdate(found.getCouponNo())
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
        claim(coupon, memberNo);
    }

    @Transactional
    public boolean issueWelcomeCoupon(int memberNo) {
        Coupon coupon = couponRepository
                .findFirstByNameAndStatusOrderByCouponNoDesc(WELCOME_SHIPPING_COUPON, "ACTIVE")
                .orElseGet(() -> couponRepository.save(Coupon.builder()
                        .issuerName("최고관리자")
                        .couponType(FREE_SHIPPING)
                        .name(WELCOME_SHIPPING_COUPON)
                        .benefitType("FREE_SHIPPING")
                        .benefitValue(0)
                        .validDays(30)
                        .notes("회원가입 후 30일 동안 사용할 수 있는 배송비 무료 쿠폰입니다.")
                        .status("ACTIVE")
                        .createdAt(LocalDateTime.now())
                        .build()));
        if (couponDetailsRepository.existsByCouponNoAndMemberNo(coupon.getCouponNo(), memberNo)) {
            return false;
        }
        claimByCouponNo(coupon.getCouponNo(), memberNo);
        return true;
    }

    @Transactional
    public boolean issueBirthdayCouponIfEligible(int memberNo) {
        Users user = usersRepository.findByMemberNo(memberNo).orElse(null);
        Member member = memberRepository.findById(memberNo).orElse(null);
        if (user == null || member == null || !"USER".equalsIgnoreCase(user.getRole())) return false;
        LocalDate birthDate = member.getBirthDate();
        LocalDate today = LocalDate.now();
        if (birthDate == null || birthDate.getYear() == 1900
                || birthDate.getMonthValue() != today.getMonthValue()
                || birthDate.getDayOfMonth() != today.getDayOfMonth()) return false;

        Coupon found = couponRepository.findFirstByNameAndStatusOrderByCouponNoDesc(BIRTHDAY_COUPON, "ACTIVE")
                .orElse(null);
        if (found == null || !isCurrentlyValid(found)) return false;
        LocalDateTime yearStart = today.withDayOfYear(1).atStartOfDay();
        LocalDateTime nextYearStart = today.plusYears(1).withDayOfYear(1).atStartOfDay();
        if (couponDetailsRepository.existsByCouponNoAndMemberNoAndIssuedAtBetween(
                found.getCouponNo(), memberNo, yearStart, nextYearStart)) return false;

        Coupon coupon = couponRepository.findByIdForUpdate(found.getCouponNo())
                .orElseThrow(() -> new IllegalArgumentException("생일축하 쿠폰을 찾을 수 없습니다."));
        if (couponDetailsRepository.existsByCouponNoAndMemberNoAndIssuedAtBetween(
                coupon.getCouponNo(), memberNo, yearStart, nextYearStart)) return false;
        couponDetailsRepository.save(CouponDetails.builder()
                .couponNo(coupon.getCouponNo()).memberNo(memberNo).isUsed("N")
                .issuedAt(LocalDateTime.now()).status("사용가능").build());
        return true;
    }

    @Transactional
    public int issueTodayBirthdayCoupons() {
        int issued = 0;
        for (Member member : memberRepository.findAll()) {
            if (issueBirthdayCouponIfEligible(member.getMemberNo())) issued++;
        }
        return issued;
    }

    @Transactional(readOnly = true)
    public Coupon findActiveProductCoupon(long prodNo) {
        return couponRepository.findFirstByProdNoAndCouponTypeAndStatusOrderByCouponNoDesc(
                prodNo, INDIVIDUAL_PRODUCT, "ACTIVE")
                .filter(coupon -> coupon.getShopNo() != null)
                .filter(this::isCurrentlyValid)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<CheckoutCouponOption> availableForOrder(
            int memberNo, Map<Long, Integer> productPrices, int shippingFee) {
        List<Long> productNos = new ArrayList<>(productPrices.keySet());
        int productPriceAfterDiscount = productPrices.values().stream().mapToInt(Integer::intValue).sum();
        List<CheckoutCouponOption> result = new ArrayList<>();
        for (CouponDetails detail : couponDetailsRepository
                .findByMemberNoAndIsUsedIgnoreCaseOrderByIssuedAtDesc(memberNo, "N")) {
            if ("중단".equals(detail.getStatus())) continue;
            Coupon coupon = couponRepository.findById(detail.getCouponNo()).orElse(null);
            if (coupon == null || !isCurrentlyValid(coupon) || !isApplicable(coupon, productNos)) continue;
            int applicablePrice = coupon.getProdNo() == null
                    ? productPriceAfterDiscount : productPrices.getOrDefault(coupon.getProdNo(), 0);
            int discount = estimateDiscount(coupon, productPriceAfterDiscount, shippingFee, applicablePrice);
            if (discount <= 0) continue;
            result.add(new CheckoutCouponOption(detail.getCouponDetailNo(), coupon.getName(),
                    coupon.getCouponType(), coupon.getBenefitType(), coupon.getBenefitValue(),
                    coupon.getProdNo(), discount));
        }
        return result;
    }

    @Transactional
    public AppliedCoupon useForOrder(long couponDetailNo, int memberNo, Map<Long, Integer> productPrices,
                                     int shippingFee) {
        List<Long> productNos = new ArrayList<>(productPrices.keySet());
        int productPriceAfterDiscount = productPrices.values().stream().mapToInt(Integer::intValue).sum();
        CouponDetails detail = couponDetailsRepository.findOwnedByIdForUpdate(couponDetailNo, memberNo)
                .orElseThrow(() -> new IllegalArgumentException("보유한 쿠폰을 찾을 수 없습니다."));
        if ("Y".equalsIgnoreCase(detail.getIsUsed()) || "사용".equals(detail.getIsUsed())) {
            throw new IllegalArgumentException("이미 사용한 쿠폰입니다.");
        }
        if ("중단".equals(detail.getStatus())) throw new IllegalArgumentException("중단된 쿠폰입니다.");
        Coupon coupon = couponRepository.findById(detail.getCouponNo())
                .orElseThrow(() -> new IllegalArgumentException("쿠폰 정보를 찾을 수 없습니다."));
        if (!isCurrentlyValid(coupon, detail.getIssuedAt())) {
            throw new IllegalArgumentException("사용기간이 아니거나 종료된 쿠폰입니다.");
        }
        if (!isApplicable(coupon, productNos)) throw new IllegalArgumentException("이 주문에 적용할 수 없는 쿠폰입니다.");
        int applicableProductPrice = coupon.getProdNo() == null
                ? productPriceAfterDiscount : productPrices.getOrDefault(coupon.getProdNo(), 0);
        int discount = estimateDiscount(coupon, productPriceAfterDiscount, shippingFee, applicableProductPrice);
        if (discount <= 0) throw new IllegalArgumentException("할인할 금액이 없어 쿠폰을 사용할 수 없습니다.");
        detail.use();
        return new AppliedCoupon(detail.getCouponDetailNo(), coupon.getCouponNo(), coupon.getCouponType(),
                coupon.getProdNo(), discount, FREE_SHIPPING.equals(coupon.getCouponType())
                        || "FREE_SHIPPING".equalsIgnoreCase(coupon.getBenefitType()));
    }

    private void claim(Coupon coupon, int memberNo) {
        if (!isCurrentlyValid(coupon)) throw new IllegalArgumentException("현재 발급할 수 없는 쿠폰입니다.");
        if (couponDetailsRepository.existsByCouponNoAndMemberNo(coupon.getCouponNo(), memberNo)) {
            throw new IllegalArgumentException("이미 발급받은 쿠폰입니다.");
        }
        couponDetailsRepository.save(CouponDetails.builder()
                .couponNo(coupon.getCouponNo()).memberNo(memberNo).isUsed("N")
                .issuedAt(LocalDateTime.now()).status("사용가능").build());
    }

    private boolean isCurrentlyValid(Coupon coupon) {
        return isCurrentlyValid(coupon, LocalDateTime.now());
    }

    private boolean isCurrentlyValid(Coupon coupon, LocalDateTime issuedAt) {
        if (!"ACTIVE".equalsIgnoreCase(coupon.getStatus()) && !"발급중".equals(coupon.getStatus())) return false;
        LocalDate today = LocalDate.now();
        if (coupon.getStartDate() != null && coupon.getStartDate().isAfter(today)) return false;
        if (coupon.getEndDate() != null && coupon.getEndDate().isBefore(today)) return false;
        return coupon.getValidDays() == null || issuedAt == null
                || !issuedAt.toLocalDate().plusDays(coupon.getValidDays()).isBefore(today);
    }

    private boolean isApplicable(Coupon coupon, List<Long> productNos) {
        if (INDIVIDUAL_PRODUCT.equals(coupon.getCouponType())) {
            return coupon.getProdNo() != null && productNos.contains(coupon.getProdNo());
        }
        return ORDER_DISCOUNT.equals(coupon.getCouponType()) || FREE_SHIPPING.equals(coupon.getCouponType());
    }

    private int estimateDiscount(Coupon coupon, int orderProductPrice, int shippingFee, int applicableProductPrice) {
        if (FREE_SHIPPING.equals(coupon.getCouponType())
                || "FREE_SHIPPING".equalsIgnoreCase(coupon.getBenefitType())) return Math.max(shippingFee, 0);
        int base = INDIVIDUAL_PRODUCT.equals(coupon.getCouponType())
                ? Math.max(applicableProductPrice, 0) : Math.max(orderProductPrice, 0);
        if ("RATE".equalsIgnoreCase(coupon.getBenefitType())) {
            return Math.min(base, (int) (base * Math.max(0, Math.min(coupon.getBenefitValue(), 100)) / 100L));
        }
        return Math.min(base, Math.max(coupon.getBenefitValue(), 0));
    }

    public record CheckoutCouponOption(long couponDetailNo, String name, String couponType,
                                       String benefitType, int benefitValue, Long prodNo, int estimatedDiscount) {}

    public record AppliedCoupon(long couponDetailNo, long couponNo, String couponType,
                                Long prodNo, int discount, boolean freeShipping) {}
}
