package org.example.k_market.service.member;

import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.Member;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.MemberRepository;
import org.example.k_market.repository.UsersRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberLoginActivityService {

    private static final long DORMANT_AFTER_DAYS = 90;

    private final UsersRepository usersRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public boolean recordSuccessfulLogin(String loginId) {
        Users user = usersRepository.findById(loginId)
                .orElseThrow(() -> new DisabledException("로그인 정보를 찾을 수 없습니다."));

        if (MemberAccountStatus.isBlockedForLogin(user.getStatus())) {
            throw new DisabledException(MemberAccountStatus.loginBlockMessage(user.getStatus()));
        }

        return memberRepository.findById(user.getMemberNo())
                .map(member -> recordMemberLogin(user, member))
                .orElse(false);
    }

    private boolean recordMemberLogin(Users user, Member member) {
        MemberAccountStatus memberStatus = member.getStatus();
        if (memberStatus != null && MemberAccountStatus.isBlockedForLogin(memberStatus)) {
            usersRepository.updateStatus(user.getMemberNo(), memberStatus);
            throw new DisabledException(MemberAccountStatus.loginBlockMessage(memberStatus));
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastLoginAt = member.getLastLoginAt();
        boolean dormantReleased = user.getStatus() == MemberAccountStatus.INACTIVE
                || memberStatus == MemberAccountStatus.INACTIVE
                || isDormantTarget(lastLoginAt, now);

        if (dormantReleased) {
            usersRepository.updateStatus(user.getMemberNo(), MemberAccountStatus.ACTIVE);
            memberRepository.updateStatus(user.getMemberNo(), MemberAccountStatus.ACTIVE);
        }

        memberRepository.updateLastLoginAt(user.getMemberNo(), now);
        return dormantReleased;
    }

    private boolean isDormantTarget(LocalDateTime lastLoginAt, LocalDateTime now) {
        if (lastLoginAt == null) {
            return false;
        }
        LocalDateTime dormantCutoff = now.minusDays(DORMANT_AFTER_DAYS);
        return !lastLoginAt.isAfter(dormantCutoff);
    }
}
