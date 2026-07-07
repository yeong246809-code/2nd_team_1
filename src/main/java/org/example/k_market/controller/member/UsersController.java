package org.example.k_market.controller.member;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.service.member.EmailVerificationService;
import org.example.k_market.service.member.UsersService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

@Log4j2
@Controller
@RequiredArgsConstructor
public class UsersController {

    private static final long EMAIL_CODE_TTL_MILLIS = 5 * 60 * 1000;
    private static final String EMAIL_SESSION_KEY = "emailVerification.email";
    private static final String EMAIL_CODE_SESSION_KEY = "emailVerification.code";
    private static final String EMAIL_EXPIRES_SESSION_KEY = "emailVerification.expiresAt";
    private static final String EMAIL_VERIFIED_SESSION_KEY = "emailVerification.verified";
    private static final String EMAIL_VERIFIED_ADDRESS_SESSION_KEY = "emailVerification.verifiedAddress";

    private final UsersService usersService;
    private final EmailVerificationService emailVerificationService;

    @GetMapping("/member/login")
    public String loginPage() {
        return "member/login";
    }

    @GetMapping("/member/session")
    @ResponseBody
    public Map<String, Object> session(Authentication authentication, HttpSession session) {
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

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("authenticated", authenticated);
        result.put("username", authenticated ? authentication.getName() : "");
        result.put("sessUser", authenticated ? sessUser : "");
        result.put("role", role);
        result.put("admin", "ADMIN".equalsIgnoreCase(role));
        return result;
    }

    @GetMapping("/member/check-id")
    @ResponseBody
    public Map<String, Object> checkId(@RequestParam String id) {
        boolean valid = id != null && id.matches("^[A-Za-z0-9]{4,16}$");
        boolean available = valid && usersService.isIdAvailable(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("valid", valid);
        result.put("available", available);
        if (!valid) {
            result.put("message", "아이디는 영문/숫자 4~16자로 입력해주세요.");
        } else if (available) {
            result.put("message", "사용 가능한 아이디입니다.");
        } else {
            result.put("message", "이미 사용 중인 아이디입니다.");
        }
        return result;
    }

    @PostMapping("/member/email/send")
    @ResponseBody
    public Map<String, Object> sendEmailVerification(@RequestParam String email, HttpSession session) {
        String targetEmail = email == null ? "" : email.trim();
        if (!isValidEmail(targetEmail)) {
            return json(false, "이메일 주소를 정확히 입력해주세요.");
        }

        String code = emailVerificationService.createCode();
        try {
            emailVerificationService.sendVerificationCode(targetEmail, code);
        } catch (RuntimeException e) {
            log.warn("이메일 인증번호 발송 실패: {}", targetEmail, e);
            return json(false, "인증번호 발송에 실패했습니다. 메일 설정을 확인해주세요.");
        }

        session.setAttribute(EMAIL_SESSION_KEY, targetEmail);
        session.setAttribute(EMAIL_CODE_SESSION_KEY, code);
        session.setAttribute(EMAIL_EXPIRES_SESSION_KEY, System.currentTimeMillis() + EMAIL_CODE_TTL_MILLIS);
        session.removeAttribute(EMAIL_VERIFIED_SESSION_KEY);
        session.removeAttribute(EMAIL_VERIFIED_ADDRESS_SESSION_KEY);

        return json(true, "인증번호를 이메일로 발송했습니다.");
    }

    @PostMapping("/member/email/verify")
    @ResponseBody
    public Map<String, Object> verifyEmailCode(
            @RequestParam String email,
            @RequestParam String code,
            HttpSession session) {
        String targetEmail = email == null ? "" : email.trim();
        String inputCode = code == null ? "" : code.trim();
        String savedEmail = (String) session.getAttribute(EMAIL_SESSION_KEY);
        String savedCode = (String) session.getAttribute(EMAIL_CODE_SESSION_KEY);
        Long expiresAt = (Long) session.getAttribute(EMAIL_EXPIRES_SESSION_KEY);

        if (savedEmail == null || savedCode == null || expiresAt == null) {
            return json(false, "인증번호를 먼저 발송해주세요.");
        }
        if (System.currentTimeMillis() > expiresAt) {
            clearEmailVerification(session);
            return json(false, "인증번호가 만료되었습니다. 다시 발송해주세요.");
        }
        if (!savedEmail.equalsIgnoreCase(targetEmail)) {
            return json(false, "인증번호를 발송한 이메일과 다릅니다.");
        }
        if (!savedCode.equals(inputCode)) {
            return json(false, "인증번호가 일치하지 않습니다.");
        }

        session.setAttribute(EMAIL_VERIFIED_SESSION_KEY, true);
        session.setAttribute(EMAIL_VERIFIED_ADDRESS_SESSION_KEY, savedEmail);
        return json(true, "이메일 인증이 완료되었습니다.");
    }

    @PostMapping("/member/register")
    public String register(
            @RequestParam String id,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(name = "password_confirm") String passwordConfirm,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!isEmailVerified(email, session)) {
            redirectAttributes.addAttribute("joinError", "이메일 인증을 완료해주세요.");
            return "redirect:/member/register";
        }

        String result = registerUser(id, password, passwordConfirm, "USER", "user", "/member/register", redirectAttributes);
        if (result.startsWith("redirect:/member/welcome")) {
            clearEmailVerification(session);
        }
        return result;
    }

    @PostMapping("/member/registerseller")
    public String registerSeller(
            @RequestParam String id,
            @RequestParam String password,
            @RequestParam(name = "password_confirm") String passwordConfirm,
            RedirectAttributes redirectAttributes) {
        return registerUser(id, password, passwordConfirm, "SELLER", "seller", "/member/registerseller", redirectAttributes);
    }

    private String registerUser(
            String id,
            String password,
            String passwordConfirm,
            String role,
            String type,
            String failUrl,
            RedirectAttributes redirectAttributes) {
        try {
            usersService.register(id, password, passwordConfirm, role);
            redirectAttributes.addAttribute("id", id.trim());
            redirectAttributes.addAttribute("type", type);
            return "redirect:/member/welcome";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute("joinError", e.getMessage());
            return "redirect:" + failUrl;
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    private boolean isEmailVerified(String email, HttpSession session) {
        String verifiedEmail = (String) session.getAttribute(EMAIL_VERIFIED_ADDRESS_SESSION_KEY);
        return Boolean.TRUE.equals(session.getAttribute(EMAIL_VERIFIED_SESSION_KEY))
                && verifiedEmail != null
                && verifiedEmail.equalsIgnoreCase(email == null ? "" : email.trim());
    }

    private void clearEmailVerification(HttpSession session) {
        session.removeAttribute(EMAIL_SESSION_KEY);
        session.removeAttribute(EMAIL_CODE_SESSION_KEY);
        session.removeAttribute(EMAIL_EXPIRES_SESSION_KEY);
        session.removeAttribute(EMAIL_VERIFIED_SESSION_KEY);
        session.removeAttribute(EMAIL_VERIFIED_ADDRESS_SESSION_KEY);
    }

    private Map<String, Object> json(boolean success, String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", success);
        result.put("message", message);
        return result;
    }

}
