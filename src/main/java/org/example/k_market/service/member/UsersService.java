package org.example.k_market.service.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.entity.Member;
import org.example.k_market.entity.Shop;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.MemberRepository;
import org.example.k_market.repository.ShopRepository;
import org.example.k_market.repository.UsersRepository;
import org.example.k_market.security.MyUserDetails;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@Service
public class UsersService implements UserDetailsService {

    private static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.of(1900, 1, 1);

    private final UsersRepository userRepository;
    private final MemberRepository memberRepository;
    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;
    private final DefaultMemberGradeService defaultMemberGradeService;

    public record FoundAccount(String id, String name, String email, String createdAt) {
    }

    public boolean isIdAvailable(String id) {
        return id != null && !id.isBlank() && userRepository.findById(id.trim()).isEmpty();
    }

    public Optional<FoundAccount> findUserIdByEmail(String name, String email) {
        String safeName = trimToNull(name);
        String safeEmail = trimToNull(email);
        if (safeName == null || safeEmail == null) {
            return Optional.empty();
        }
        return memberRepository.findFirstByNameAndEmailIgnoreCase(safeName, safeEmail)
                .flatMap(this::toFoundAccount);
    }

    public Optional<FoundAccount> findUserIdByPhone(String name, String phone) {
        String safeName = trimToNull(name);
        String safePhone = normalizePhone(phone);
        if (safeName == null || safePhone == null) {
            return Optional.empty();
        }
        return memberRepository.findFirstByNameAndNormalizedPhone(safeName, safePhone)
                .flatMap(this::toFoundAccount);
    }

    public Optional<Users> findPasswordResetTargetByEmail(String id, String email) {
        String safeId = trimToNull(id);
        String safeEmail = trimToNull(email);
        if (safeId == null || safeEmail == null) {
            return Optional.empty();
        }
        return userRepository.findById(safeId)
                .filter(user -> memberRepository.findById(user.getMemberNo())
                        .map(Member::getEmail)
                        .map(memberEmail -> memberEmail.equalsIgnoreCase(safeEmail))
                        .orElse(false));
    }

    public Optional<Users> findPasswordResetTargetByPhone(String id, String phone) {
        String safeId = trimToNull(id);
        String safePhone = normalizePhone(phone);
        if (safeId == null || safePhone == null) {
            return Optional.empty();
        }
        return userRepository.findById(safeId)
                .filter(user -> memberRepository.findById(user.getMemberNo())
                        .map(Member::getPhone)
                        .map(this::normalizePhone)
                        .map(safePhone::equals)
                        .orElse(false));
    }

    @Transactional
    public void resetPassword(String id, String password, String passwordConfirm) {
        String safeId = trimToNull(id);
        if (safeId == null) {
            throw new IllegalArgumentException("아이디를 확인해주세요.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("새 비밀번호를 입력해주세요.");
        }
        if (!password.equals(passwordConfirm)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if (password.length() < 4) {
            throw new IllegalArgumentException("비밀번호는 4자 이상 입력해주세요.");
        }
        if (userRepository.findById(safeId).isEmpty()) {
            throw new IllegalArgumentException("아이디를 찾을 수 없습니다.");
        }
        userRepository.updatePasswordByLoginId(safeId, passwordEncoder.encode(password));
    }

    @Transactional
    public void changePassword(String id, String currentPassword, String newPassword, String passwordConfirm) {
        String safeId = trimToNull(id);
        if (safeId == null) {
            throw new IllegalArgumentException("로그인 정보를 확인할 수 없습니다.");
        }
        Users user = userRepository.findById(safeId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPass())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("새 비밀번호를 입력해주세요.");
        }
        if (newPassword.length() < 4) {
            throw new IllegalArgumentException("비밀번호는 4자 이상 입력해주세요.");
        }
        if (!newPassword.equals(passwordConfirm)) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        if (passwordEncoder.matches(newPassword, user.getPass())) {
            throw new IllegalArgumentException("현재 비밀번호와 다른 비밀번호를 입력해주세요.");
        }
        userRepository.updatePasswordByLoginId(safeId, passwordEncoder.encode(newPassword));
    }

    @Transactional
    public Users register(String id, String password, String passwordConfirm, String role) {
        Users user = createUser(id, password, passwordConfirm, role);
        if ("USER".equalsIgnoreCase(user.getRole())) {
            saveMemberProfile(user, null, null, null, null, null, null);
        } else if ("SELLER".equalsIgnoreCase(user.getRole())) {
            saveShopProfile(user, null, null, null, null, null, null, null, null, null);
        }
        return user;
    }

    @Transactional
    public Users registerUser(
            String id,
            String password,
            String passwordConfirm,
            String email,
            String name,
            String phone,
            String zipCode,
            String baseAddress,
            String detailAddress) {
        Users user = createUser(id, password, passwordConfirm, "USER");
        saveMemberProfile(user, email, name, phone, zipCode, baseAddress, detailAddress);
        return user;
    }

    @Transactional
    public Users registerSeller(
            String id,
            String password,
            String passwordConfirm,
            String company,
            String representative,
            String licenseNumber,
            String reportNumber,
            String phone,
            String fax,
            String zipCode,
            String baseAddress,
            String detailAddress) {
        Users user = createUser(id, password, passwordConfirm, "SELLER");
        saveShopProfile(user, company, representative, licenseNumber, reportNumber, phone, fax, zipCode, baseAddress, detailAddress);
        return user;
    }

    private Users createUser(String id, String password, String passwordConfirm, String role) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("아이디를 입력해주세요.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }
        if (!password.equals(passwordConfirm)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if (userRepository.findById(id.trim()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        Users user = Users.builder()
                .id(id.trim())
                .pass(passwordEncoder.encode(password))
                .role(normalizeRole(role))
                .status(MemberAccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();

        return userRepository.save(user);
    }

    private void saveMemberProfile(
            Users user,
            String email,
            String name,
            String phone,
            String zipCode,
            String baseAddress,
            String detailAddress) {
        Member member = Member.builder()
                .memberNo(user.getMemberNo())
                .name(trimToNull(name))
                .birthDate(DEFAULT_BIRTH_DATE)
                .email(trimToNull(email))
                .phone(trimToNull(phone))
                .zipCode(trimToNull(zipCode))
                .baseAddress(valueOrEmpty(baseAddress))
                .detailAddress(trimToNull(detailAddress))
                .gradeNo(defaultMemberGradeService.getDefaultGradeNo())
                .points(0)
                .createdAt(LocalDateTime.now())
                .status(MemberAccountStatus.ACTIVE)
                .locationPolicyAgreed("Y")
                .build();

        memberRepository.save(member);
    }

    private void saveShopProfile(
            Users user,
            String company,
            String representative,
            String licenseNumber,
            String reportNumber,
            String phone,
            String fax,
            String zipCode,
            String baseAddress,
            String detailAddress) {
        Shop shop = Shop.builder()
                .memberNo(user.getMemberNo())
                .name(trimToNull(company))
                .ceo(trimToNull(representative))
                .bizNumber(trimToNull(licenseNumber))
                .mailOrderNumber(trimToNull(reportNumber))
                .phone(trimToNull(phone))
                .fax(trimToNull(fax))
                .zipCode(trimToNull(zipCode))
                .baseAddress(valueOrEmpty(baseAddress))
                .detailAddress(valueOrEmpty(detailAddress))
                .status("PENDING")
                .rdate(LocalDateTime.now())
                .build();

        shopRepository.save(shop);
    }

    private String normalizeRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return "USER";
        }
        return role.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private Optional<FoundAccount> toFoundAccount(Member member) {
        return userRepository.findByMemberNo(member.getMemberNo())
                .map(user -> new FoundAccount(
                        user.getId(),
                        valueOrEmpty(member.getName()),
                        valueOrEmpty(member.getEmail()),
                        user.getCreatedAt()
                ));
    }

    private String normalizePhone(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        String normalized = trimmed.replaceAll("[^0-9]", "");
        return normalized.isBlank() ? null : normalized;
    }

    private String valueOrEmpty(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? "" : trimmed;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("로그인 시도 아이디: " + username);

        Optional<Users> optUser = userRepository.findById(username);

        if (optUser.isPresent()){
            Users user = optUser.get();
            log.info("사용자 발견: " + user.getId());

            if (MemberAccountStatus.isBlockedForLogin(user.getStatus())) {
                throw new DisabledException(MemberAccountStatus.loginBlockMessage(user.getStatus()));
            }

            MyUserDetails details = MyUserDetails.builder()
                    .user(user)
                    .build();

            return details;
        }

        log.info("사용자를 찾을 수 없음!");
        throw new UsernameNotFoundException("해당 아이디를 찾을 수 없습니다: " + username);
    }
}
