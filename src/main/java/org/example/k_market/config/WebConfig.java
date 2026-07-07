package org.example.k_market.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 기존 코드 지우고 아래 코드로 변경
    private final String UPLOAD_PATH = new File("uploads/").getAbsolutePath() + File.separator;

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
}