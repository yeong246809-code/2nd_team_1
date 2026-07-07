package org.example.k_market.service.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.example.k_market.entity.Users;
import org.example.k_market.repository.UsersRepository;
import org.example.k_market.security.MyUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@Service
public class UsersService implements UserDetailsService {

    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean isIdAvailable(String id) {
        return id != null && !id.isBlank() && userRepository.findById(id.trim()).isEmpty();
    }

    @Transactional
    public Users register(String id, String password, String passwordConfirm, String role) {
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
                .role(role)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();

        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("로그인 시도 아이디: " + username);

        // ★ 중요: UserRepository에 findById(String id) 메서드가 반드시 있어야 합니다.
        // 만약 memberNo로만 찾을 수 있다면, 레포지토리에 findById(String id)를 추가해주세요.
        Optional<Users> optUser = userRepository.findById(username);

        if (optUser.isPresent()){
            log.info("사용자 발견: " + optUser.get().getId());

            MyUserDetails details = MyUserDetails.builder()
                    .user(optUser.get())
                    .build();

            return details;
        }

        log.info("사용자를 찾을 수 없음!");
        throw new UsernameNotFoundException("해당 아이디를 찾을 수 없습니다: " + username);
    }
}
