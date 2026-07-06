package org.example.k_market.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        // 로그인 설정
        httpSecurity.formLogin( form -> form
                .loginPage("/member/login")
                .loginProcessingUrl("/member/login")
                .defaultSuccessUrl("/admin/index") // 로그인 성공 시 이동할 관리자 메인 페이지
                .failureUrl("/member/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
        );

        // 로그아웃 설정
        httpSecurity.logout( config -> config
                .logoutUrl("/member/logout")
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/member/login?logout=success")
        );

        // 인가 설정 (관리자 전용 접근 제어)
        httpSecurity.authorizeHttpRequests( authorize -> authorize
                // 로그인 페이지와 정적 리소스들을 명확히 분리해서 선언하세요
                .requestMatchers("/member/login", "/resources/**", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
        );

        // CSRF 비활성화 (필요에 따라 켜셔도 됩니다)
        httpSecurity.csrf(CsrfConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        // ★ 비밀번호 평문 비교를 위한 설정 (운영 환경에서는 절대 사용 금지)
        return NoOpPasswordEncoder.getInstance();
    }
}