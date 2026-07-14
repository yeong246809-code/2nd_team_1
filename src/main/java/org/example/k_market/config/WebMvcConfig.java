package org.example.k_market.config;

import lombok.RequiredArgsConstructor;
import org.example.k_market.security.SellerShopAccessInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final SellerShopAccessInterceptor sellerShopAccessInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sellerShopAccessInterceptor)
                .addPathPatterns("/admin/product/**", "/admin/order/**");
    }
}
