package org.example.k_market.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("role");

        if (role == null || !role.equals("ADMIN")) {
            response.sendRedirect(request.getContextPath() + "/member/login");
            return false;
        }
        return true;
    }
}