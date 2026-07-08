package org.example.k_market.service.member;

import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.MemberRepository;
import org.example.k_market.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberWithdrawalService {

    private static final String WITHDRAWAL_MEMO = "회원 탈퇴";

    private final UsersRepository usersRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void withdraw(String loginId) {
        Users user = usersRepository.findById(loginId)
                .orElseThrow(() -> new IllegalArgumentException("로그인 정보를 찾을 수 없습니다."));

        usersRepository.updateStatus(user.getMemberNo(), MemberAccountStatus.WITHDRAWN);
        memberRepository.updateWithdrawalStatus(
                user.getMemberNo(),
                MemberAccountStatus.WITHDRAWN,
                WITHDRAWAL_MEMO);
    }
}
