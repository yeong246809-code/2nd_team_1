package org.example.k_market.service;

import org.example.k_market.entity.Member;
import org.example.k_market.entity.PointHistory;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.MemberRepository;
import org.example.k_market.repository.PointHistoryRepository;
import org.example.k_market.repository.CouponDetailsRepository;
import org.example.k_market.repository.CouponRepository;
import org.example.k_market.repository.UsersRepository;
import org.example.k_market.entity.Coupon;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.example.k_market.service.member.SnsLoginService;
import org.example.k_market.service.member.SnsProfile;
import org.example.k_market.service.member.SnsProvider;
import org.example.k_market.service.member.UsersService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SignupPointServiceTest {
    @Autowired UsersService usersService;
    @Autowired SnsLoginService snsLoginService;
    @Autowired MemberRepository memberRepository;
    @Autowired PointHistoryRepository pointHistoryRepository;
    @Autowired CouponDetailsRepository couponDetailsRepository;
    @Autowired CouponRepository couponRepository;
    @Autowired UsersRepository usersRepository;
    @Autowired CouponIssuanceService couponIssuanceService;

    @Test
    void regularSignupReceivesThreeThousandPoints() {
        Users user = usersService.registerUser(
                "signup-bonus", "1234", "1234", "signup@test.com",
                "신규회원", LocalDate.of(2000, 7, 15),
                "010-1234-5678", "12345", "부산광역시", "101호");

        assertSignupBonus(user.getMemberNo());
        assertThat(memberRepository.findById(user.getMemberNo()).orElseThrow().getBirthDate())
                .isEqualTo(LocalDate.of(2000, 7, 15));
    }

    @Test
    void newSnsSignupReceivesThreeThousandPointsOnlyOnCreation() {
        SnsProfile profile = new SnsProfile(
                SnsProvider.KAKAO, "signup-sns", "sns@test.com", "SNS회원", "010-9999-9999");

        Users created = snsLoginService.findOrCreateUser(profile);
        Users foundAgain = snsLoginService.findOrCreateUser(profile);

        assertThat(foundAgain.getMemberNo()).isEqualTo(created.getMemberNo());
        assertSignupBonus(created.getMemberNo());
        assertThat(pointHistoryRepository.findByMemberNoOrderByCreatedAtDesc(
                created.getMemberNo(), PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
    }

    private void assertSignupBonus(int memberNo) {
        Member member = memberRepository.findById(memberNo).orElseThrow();
        PointHistory history = pointHistoryRepository.findByMemberNoOrderByCreatedAtDesc(
                memberNo, PageRequest.of(0, 1)).getContent().get(0);

        assertThat(member.getPoints()).isEqualTo(3_000);
        assertThat(history.getAmount()).isEqualTo(3_000);
        assertThat(history.getRemainedAmount()).isEqualTo(3_000);
        assertThat(history.getDescription()).isEqualTo("신규 회원가입 축하 포인트");
        var issuedCoupons = couponDetailsRepository.findByMemberNoOrderByIssuedAtDesc(
                memberNo, PageRequest.of(0, 10));
        assertThat(issuedCoupons.getTotalElements()).isEqualTo(1);
        assertThat(couponRepository.findById(issuedCoupons.getContent().get(0).getCouponNo()))
                .get().extracting(org.example.k_market.entity.Coupon::getName)
                .isEqualTo(CouponIssuanceService.WELCOME_SHIPPING_COUPON);
    }

    @Test
    void birthdayCouponIsIssuedOncePerYearOnlyToRegularUser() {
        Coupon birthdayCoupon = couponRepository.save(Coupon.builder()
                .issuerName("최고관리자").couponType(CouponIssuanceService.ORDER_DISCOUNT)
                .name(CouponIssuanceService.BIRTHDAY_MEMORIAL_COUPON).benefitType("AMOUNT").benefitValue(3000)
                .status("ACTIVE").createdAt(LocalDateTime.now()).build());
        Users regular = usersRepository.save(Users.builder().id("birthday-user").pass("x").role("USER").build());
        memberRepository.save(Member.builder().memberNo(regular.getMemberNo()).name("생일회원")
                .birthDate(LocalDate.now()).points(0).build());
        Users seller = usersRepository.save(Users.builder().id("birthday-seller").pass("x").role("SELLER").build());
        memberRepository.save(Member.builder().memberNo(seller.getMemberNo()).name("생일판매자")
                .birthDate(LocalDate.now()).points(0).build());

        assertThat(couponIssuanceService.issueBirthdayCouponIfEligible(regular.getMemberNo())).isTrue();
        assertThat(couponIssuanceService.issueBirthdayCouponIfEligible(regular.getMemberNo())).isFalse();
        assertThat(couponIssuanceService.issueBirthdayCouponIfEligible(seller.getMemberNo())).isFalse();
        assertThat(couponDetailsRepository.countByCouponNo(birthdayCoupon.getCouponNo())).isEqualTo(1);
    }
}
