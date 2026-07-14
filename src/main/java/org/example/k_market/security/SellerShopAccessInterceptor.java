package org.example.k_market.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.Shop;
import org.example.k_market.repository.ShopRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class SellerShopAccessInterceptor implements HandlerInterceptor {

    private final ShopRepository shopRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(authority -> "ROLE_SELLER".equals(authority.getAuthority()))) {
            return true;
        }
        if (!(authentication.getPrincipal() instanceof MyUserDetails userDetails)) {
            throw new AccessDeniedException("판매자 정보를 확인할 수 없습니다.");
        }
        Shop shop = shopRepository.findById(userDetails.getUser().getMemberNo())
                .orElseThrow(() -> new AccessDeniedException("판매자 상점 정보를 확인할 수 없습니다."));
        if (!"ACTIVE".equalsIgnoreCase(shop.getStatus())) {
            if ("PENDING".equalsIgnoreCase(shop.getStatus())) {
                throw new AccessDeniedException("운영 준비중인 판매자는 관리자 페이지를 이용할 수 없습니다.");
            }
            throw new AccessDeniedException("운영 중지된 판매자는 관리자 페이지를 이용할 수 없습니다.");
        }
        return true;
    }
}
