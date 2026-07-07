package org.example.k_market.controller.my;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
@RequiredArgsConstructor
public class MyIndexController {

    @GetMapping("/my/index")
    public String index(Authentication authentication, HttpSession session, Model model) {
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(String.valueOf(authentication.getPrincipal()));
        
        String sessUser = authenticated ? String.valueOf(session.getAttribute("sessUser")) : "";
        if (authenticated && (sessUser == null || sessUser.isBlank() || "null".equals(sessUser))) {
            sessUser = authentication.getName();
            session.setAttribute("sessUser", sessUser);
        }
        String role = "";
        if (authenticated) {
            role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(authority -> authority.startsWith("ROLE_"))
                    .map(authority -> authority.substring("ROLE_".length()))
                    .findFirst()
                    .orElse("");
        }

        model.addAttribute("loginUserId", authenticated ? sessUser : "");
        model.addAttribute("loginUserRole", role.isBlank() ? "USER" : role);
        model.addAttribute("isAdmin", "ADMIN".equalsIgnoreCase(role));
        return "my/index";
    }
}
