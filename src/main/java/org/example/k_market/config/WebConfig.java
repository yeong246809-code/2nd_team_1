package org.example.k_market.config;

import lombok.RequiredArgsConstructor;
import org.example.k_market.interceptor.VisitorInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    // 기존 코드 지우고 아래 코드로 변경
    private final String UPLOAD_PATH = new File("uploads/").getAbsolutePath() + File.separator;
    private final VisitorInterceptor visitorInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 경로가 실제로 존재하는지 확인하고 없으면 폴더 생성 (선택사항)
        File dir = new File(UPLOAD_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 웹 브라우저에서 /uploads/~~~ 로 요청이 오면,
        // 실제 컴퓨터의 최상위 폴더/uploads/ 에서 파일을 찾아서 보여주도록 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + UPLOAD_PATH);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(visitorInterceptor)
                .addPathPatterns("/**")                  // 모든 경로에서 방문자 감지
                .excludePathPatterns(
                        "/admin/**",                     // 관리자 페이지 접속은 제외
                        "/css/**", "/js/**", "/images/**", "/favicon.ico", "/error", // 정적 리소스
                        "/uploads/**", "/font/**", "/assets/**", "*.png", "*.jpg", "*.jpeg", "*.gif", // 이미지 파일 제외
                        "/member/session", "/api/**"     // 백그라운드 API 통신 및 세션 확인 요청 제외!
                );
    }
}