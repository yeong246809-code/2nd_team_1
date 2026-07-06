package org.example.k_market.service.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.example.k_market.entity.Users;
import org.example.k_market.repository.UsersRepository;
import org.example.k_market.security.MyUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@Service
public class UsersService implements UserDetailsService {

    private final UsersRepository userRepository;

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