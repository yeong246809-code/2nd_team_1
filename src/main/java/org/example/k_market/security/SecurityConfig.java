package org.example.k_market.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.PrintWriter;

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        // 로그인 설정
        httpSecurity.formLogin( form -> form
                .loginPage("/member/login")
                .loginProcessingUrl("/member/login")
                .failureUrl("/member/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    // 💡 현재 애플리케이션의 Context Path 가져오기 (예: "/k_market")
                    String contextPath = request.getContextPath();

                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                    if (isAdmin) {
                        // Context Path를 앞에 붙여서 리다이렉트
                        response.sendRedirect(contextPath + "/admin/index");
                    } else {
                        // 일반 유저 메인 페이지
                        response.sendRedirect(contextPath + "/my/index");
                    }
                })
        );

        // 로그아웃 설정
        httpSecurity.logout( config -> config
                .logoutRequestMatcher(new AntPathRequestMatcher("/member/logout", "GET"))
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/member/login?logout=success")
        );

        // 인가 설정 (권한 제어)
        httpSecurity.authorizeHttpRequests( authorize -> authorize
                .requestMatchers(
                        "/member/login", "/member/join", "/member/signup", "/member/welcome",
                        "/member/session", "/member/check-id", "/member/email/send", "/member/email/verify",
                        "/member/register", "/member/registerseller",
                        "/resources/**", "/css/**", "/js/**", "/images/**", "/uploads/**"
                ).permitAll()
                //셀러 권한 필요하면 여기에 추가
                .requestMatchers("/admin/product/**").hasAnyRole("ADMIN", "SELLER")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
        );

        // 예외 처리 (JavaScript로 alert 띄우고 리다이렉트)
        httpSecurity.exceptionHandling( exception -> exception
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    String contextPath = request.getContextPath();

                    response.setContentType("text/html; charset=UTF-8");
                    PrintWriter out = response.getWriter();
                    out.print("<script>");
                    out.print("alert('접근할 수 없는 페이지입니다.');");
                    out.print("location.href='" + contextPath + "/';");
                    out.print("</script>");
                    out.flush();
                })
        );

        // CSRF 비활성화
        httpSecurity.csrf(CsrfConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }
}