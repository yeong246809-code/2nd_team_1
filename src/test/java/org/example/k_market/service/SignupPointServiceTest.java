package org.example.k_market.service;

import org.example.k_market.entity.Member;
import org.example.k_market.entity.PointHistory;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.MemberRepository;
import org.example.k_market.repository.PointHistoryRepository;
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

    @Test
    void regularSignupReceivesThreeThousandPoints() {
        Users user = usersService.registerUser(
                "signup-bonus", "1234", "1234", "signup@test.com",
                "신규회원", "010-1234-5678", "12345", "부산광역시", "101호");

        assertSignupBonus(user.getMemberNo());
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
    }
}
