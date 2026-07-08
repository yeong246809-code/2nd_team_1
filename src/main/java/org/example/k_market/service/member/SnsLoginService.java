package org.example.k_market.service.member;

import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.Member;
import org.example.k_market.entity.SnsAccount;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.MemberRepository;
import org.example.k_market.repository.SnsAccountRepository;
import org.example.k_market.repository.UsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SnsLoginService {

    private static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.of(1900, 1, 1);
    private static final int NAVER_LOGIN_ID_MAX_LENGTH = 8;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SnsAccountRepository snsAccountRepository;
    private final UsersRepository usersRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final DefaultMemberGradeService defaultMemberGradeService;

    @Transactional
    public Users findOrCreateUser(SnsProfile profile) {
        return snsAccountRepository.findByProviderAndProviderId(
                        profile.provider().registrationId(),
                        profile.providerId())
                .map(snsAccount -> usersRepository.findById(snsAccount.getMemberNo())
                        .map(user -> {
                            blockLoginIfNeeded(user);
                            return compactExistingLoginId(profile, user);
                        })
                        .orElseThrow(() -> new IllegalStateException("SNS 계정에 연결된 회원 정보를 찾을 수 없습니다.")))
                .orElseGet(() -> createSocialUser(profile));
    }

    private Users createSocialUser(SnsProfile profile) {
        Users user = Users.builder()
                .id(createLoginId(profile, 0))
                .pass(createSocialOnlyPassword())
                .role("USER")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();

        Users savedUser = usersRepository.save(user);
        saveMemberProfile(savedUser, profile);
        saveSnsAccount(savedUser, profile);
        return savedUser;
    }

    private void saveMemberProfile(Users user, SnsProfile profile) {
        Member member = Member.builder()
                .memberNo(user.getMemberNo())
                .name(profile.displayName())
                .birthDate(DEFAULT_BIRTH_DATE)
                .email(profile.email())
                .phone(profile.phone())
                .baseAddress("")
                .gradeNo(defaultMemberGradeService.getDefaultGradeNo())
                .points(0)
                .createdAt(LocalDateTime.now())
                .status("ACTIVE")
                .locationPolicyAgreed("N")
                .build();

        memberRepository.save(member);
    }

    private void saveSnsAccount(Users user, SnsProfile profile) {
        SnsAccount snsAccount = SnsAccount.builder()
                .memberNo(user.getMemberNo())
                .provider(profile.provider().registrationId())
                .providerId(profile.providerId())
                .connectedAt(LocalDateTime.now())
                .build();

        snsAccountRepository.save(snsAccount);
    }

    private Users compactExistingLoginId(SnsProfile profile, Users user) {
        if (profile.provider() != SnsProvider.NAVER || user.getId().length() <= NAVER_LOGIN_ID_MAX_LENGTH) {
            return user;
        }

        String compactLoginId = createLoginId(profile, user.getMemberNo());
        if (!compactLoginId.equals(user.getId())) {
            usersRepository.updateLoginId(user.getMemberNo(), compactLoginId);
            return usersRepository.findById(user.getMemberNo()).orElse(user);
        }

        return user;
    }

    private String createLoginId(SnsProfile profile, int currentMemberNo) {
        String normalizedProviderId = profile.providerId()
                .replaceAll("[^A-Za-z0-9._-]", "")
                .trim();
        if (normalizedProviderId.isEmpty()) {
            normalizedProviderId = profile.provider().registrationId();
        }

        int sequence = 0;
        while (true) {
            String loginId = buildLoginId(profile, normalizedProviderId, sequence);
            if (isLoginIdAvailable(loginId, currentMemberNo)) {
                return loginId;
            }
            sequence++;
        }
    }

    private String buildLoginId(SnsProfile profile, String normalizedProviderId, int sequence) {
        if (profile.provider() == SnsProvider.NAVER) {
            return compactNaverLoginId(normalizedProviderId, profile.provider().idSuffix(), sequence);
        }

        if (sequence == 0) {
            return normalizedProviderId + profile.provider().idSuffix();
        }
        return normalizedProviderId + "-" + sequence + profile.provider().idSuffix();
    }

    private String compactNaverLoginId(String normalizedProviderId, String suffix, int sequence) {
        String sequenceToken = sequence == 0 ? "" : Integer.toString(sequence, 36);
        int maxBaseLength = NAVER_LOGIN_ID_MAX_LENGTH - suffix.length() - sequenceToken.length();
        if (maxBaseLength < 1) {
            sequenceToken = sequenceToken.substring(sequenceToken.length() - (NAVER_LOGIN_ID_MAX_LENGTH - suffix.length() - 1));
            maxBaseLength = 1;
        }

        String base = normalizedProviderId.length() <= maxBaseLength
                ? normalizedProviderId
                : normalizedProviderId.substring(0, maxBaseLength);
        return base + sequenceToken + suffix;
    }

    private boolean isLoginIdAvailable(String loginId, int currentMemberNo) {
        return usersRepository.findById(loginId)
                .map(user -> {
                    if (user.getMemberNo() == currentMemberNo) {
                        return true;
                    }
                    if (MemberAccountStatus.isWithdrawn(user.getStatus())) {
                        throw new IllegalStateException(MemberAccountStatus.loginBlockMessage(user.getStatus()));
                    }
                    return false;
                })
                .orElse(true);
    }

    private void blockLoginIfNeeded(Users user) {
        if (MemberAccountStatus.isBlockedForLogin(user.getStatus())) {
            throw new IllegalStateException(MemberAccountStatus.loginBlockMessage(user.getStatus()));
        }
    }

    private String createSocialOnlyPassword() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        String randomPassword = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return passwordEncoder.encode(randomPassword);
    }
}
