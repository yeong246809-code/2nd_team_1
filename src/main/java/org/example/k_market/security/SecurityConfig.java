package org.example.k_market.security;

import lombok.extern.slf4j.Slf4j;
import org.example.k_market.service.member.CustomOAuth2UserService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.util.UriUtils;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@EnableMethodSecurity
@Configuration
@Slf4j
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity httpSecurity,
            CustomOAuth2UserService customOAuth2UserService,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository) throws Exception {

        // 로그인 설정
        httpSecurity.formLogin( form -> form
                .loginPage("/member/login")
                .loginProcessingUrl("/member/login")
                .failureHandler((request, response, exception) -> {
                    String message = loginFailureMessage(exception);
                    response.sendRedirect(request.getContextPath()
                            + "/member/login?error=true&loginMessage="
                            + UriUtils.encode(message, StandardCharsets.UTF_8));
                })
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    // 💡 현재 애플리케이션의 Context Path 가져오기 (예: "/k_market")
                    String contextPath = request.getContextPath();
                    String userId = authentication.getName();
                    request.getSession().setAttribute("sessUser", userId);

                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                    // 관리자 여부를 세션에 따로 저장
                    request.getSession().setAttribute("sessUser", userId);
                    request.getSession().setAttribute("isAdmin", isAdmin);

                    if (isAdmin) {
                        // Context Path를 앞에 붙여서 리다이렉트
                        response.sendRedirect(contextPath + "/admin/index");
                    } else {
                        // 일반 유저 메인 페이지
                        response.sendRedirect(contextPath + "/my/index");
                    }
                })
        );

        if (clientRegistrationRepository.getIfAvailable() != null) {
            httpSecurity.oauth2Login(oauth2 -> oauth2
                    .loginPage("/member/login")
                    .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                    .failureHandler((request, response, exception) -> {
                        String message = oauthFailureMessage(exception);
                        log.warn("SNS login failed: {}", exception.getMessage(), exception);
                        response.sendRedirect(request.getContextPath()
                                + "/member/login?error=sns&snsMessage="
                                + UriUtils.encode(message, StandardCharsets.UTF_8));
                    })
                    .successHandler((request, response, authentication) -> {
                        String contextPath = request.getContextPath();
                        String userId = authentication.getName();

                        request.getSession().setAttribute("sessUser", userId);
                        request.getSession().setAttribute("isAdmin", false);

                        response.sendRedirect(contextPath + "/my/index");
                    })
            );
        }

        // 로그아웃 설정
        httpSecurity.logout( config -> config
                .logoutRequestMatcher(new AntPathRequestMatcher("/member/logout", "GET"))
                .invalidateHttpSession(true)
                .logoutSuccessHandler((request, response, authentication) ->
                        response.sendRedirect(request.getContextPath() + "/member/login?logout=success"))
        );

        // 인가 설정 (권한 제어)
        httpSecurity.authorizeHttpRequests( authorize -> authorize
                .requestMatchers(
                        "/member/login", "/member/join", "/member/signup", "/member/welcome",
                        "/member/session", "/member/check-id", "/member/email/send", "/member/email/verify",
                        "/member/register", "/member/registerseller",
                        "/oauth2/**", "/login/oauth2/**",
                        "/resources/**", "/css/**", "/js/**", "/images/**", "/uploads/**"
                ).permitAll()
                //셀러 권한 필요하면 여기에 추가
                .requestMatchers("/admin/product/**").hasAnyRole("ADMIN", "SELLER")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/cs/notice/write").hasRole("ADMIN")
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

    private String oauthFailureMessage(Exception exception) {
        String message = exceptionChainMessage(exception);
        String lowerMessage = message.toLowerCase(Locale.ROOT);
        String accountStatusMessage = accountStatusFailureMessage(message, lowerMessage);

        if (accountStatusMessage != null) {
            return accountStatusMessage;
        }

        if (lowerMessage.contains("invalid_scope") || lowerMessage.contains("koe205")) {
            return "카카오 동의항목 설정이 맞지 않습니다. 카카오 개발자 콘솔의 동의항목을 확인해주세요.";
        }
        if (lowerMessage.contains("redirect_uri") || lowerMessage.contains("redirect uri")) {
            return "SNS Redirect URI 설정이 현재 접속 주소와 다릅니다.";
        }
        if (lowerMessage.contains("invalid_token_response")
                || lowerMessage.contains("invalid_client")
                || lowerMessage.contains("client_secret")
                || lowerMessage.contains("401")) {
            return "SNS 앱 키 또는 Client Secret 설정을 확인해주세요. 카카오는 REST API 키와 Client Secret 사용 여부가 일치해야 합니다.";
        }

        return "SNS 로그인 처리 중 오류가 발생했습니다. 서버 로그를 확인해주세요.";
    }

    private String loginFailureMessage(Exception exception) {
        String message = exceptionChainMessage(exception);
        String lowerMessage = message.toLowerCase(Locale.ROOT);
        String accountStatusMessage = accountStatusFailureMessage(message, lowerMessage);

        if (accountStatusMessage != null) {
            return accountStatusMessage;
        }
        return "아이디 또는 비밀번호를 확인해주세요.";
    }

    private String accountStatusFailureMessage(String message, String lowerMessage) {
        if (message.contains("탈퇴") || lowerMessage.contains("withdrawn") || lowerMessage.contains("deleted")) {
            return "탈퇴한 아이디라서 로그인할 수 없습니다.";
        }
        if (message.contains("중지")
                || lowerMessage.contains("suspended")
                || lowerMessage.contains("blocked")
                || lowerMessage.contains("stopped")) {
            return "중지된 계정입니다. 고객센터에 문의해주세요.";
        }
        if (message.contains("휴면") || message.contains("휴먼") || lowerMessage.contains("dormant")) {
            return "휴면 계정입니다. 고객센터에 문의해주세요.";
        }
        return null;
    }

    private String exceptionChainMessage(Throwable throwable) {
        StringBuilder message = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            if (current.getMessage() != null) {
                if (!message.isEmpty()) {
                    message.append(" ");
                }
                message.append(current.getMessage());
            }
            current = current.getCause();
        }
        return message.toString();
    }
}
