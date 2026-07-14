package org.example.k_market.controller.member;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.service.member.EmailVerificationService;
import org.example.k_market.service.member.UsersService;
import org.example.k_market.service.admin.BannerService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    private static final String PASSWORD_RESET_ID_SESSION_KEY = "passwordReset.id";

    private final UsersService usersService;
    private final EmailVerificationService emailVerificationService;
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository;
    private final BannerService bannerService;

    @GetMapping("/member/login")
    public String loginPage(
            Authentication authentication,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "loginMessage", required = false) String loginMessage,
            @RequestParam(name = "snsMessage", required = false) String snsMessage,
            Model model) {
        if (isAuthenticated(authentication)) {
            if (isAdmin(authentication)) {
                return "redirect:/admin/index";
            }
            return "redirect:/my/index";
        }

        boolean snsError = "sns".equals(error);
        model.addAttribute("snsError", snsError);
        model.addAttribute("loginError", error != null && !snsError);
        model.addAttribute("loginMessage", (loginMessage == null || loginMessage.isBlank())
                ? "아이디 또는 비밀번호를 확인해주세요."
                : loginMessage);
        model.addAttribute("snsMessage", (snsMessage == null || snsMessage.isBlank())
                ? "SNS 로그인에 실패했습니다. 설정을 확인해주세요."
                : snsMessage);
        model.addAttribute("googleLoginEnabled", hasClientRegistration("google"));
        model.addAttribute("loginBanner", bannerService.getDisplayableBanner("MEMBER1"));

        return "member/login";
    }

    @GetMapping("/member/session")
    @ResponseBody
    public Map<String, Object> session(Authentication authentication, HttpSession session) {
        boolean authenticated = isAuthenticated(authentication);

        
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
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "phone", required = false) String phone,
            @RequestParam(name = "zip", required = false) String zipCode,
            @RequestParam(name = "addr1", required = false) String baseAddress,
            @RequestParam(name = "addr2", required = false) String detailAddress,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!isEmailVerified(email, session)) {
            redirectAttributes.addAttribute("joinError", "이메일 인증을 완료해주세요.");
            return "redirect:/member/register";
        }

        try {
            usersService.registerUser(id, password, passwordConfirm, email, name, phone, zipCode, baseAddress, detailAddress);
            redirectAttributes.addAttribute("id", id.trim());
            redirectAttributes.addAttribute("type", "user");
            clearEmailVerification(session);
            return "redirect:/member/welcome";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute("joinError", e.getMessage());
            return "redirect:/member/register";
        }
    }

    @PostMapping("/member/registerseller")
    public String registerSeller(
            @RequestParam String id,
            @RequestParam String password,
            @RequestParam(name = "password_confirm") String passwordConfirm,
            @RequestParam(name = "company", required = false) String company,
            @RequestParam(name = "representative", required = false) String representative,
            @RequestParam(name = "license_number", required = false) String licenseNumber,
            @RequestParam(name = "report_number", required = false) String reportNumber,
            @RequestParam(name = "phone", required = false) String phone,
            @RequestParam(name = "fax", required = false) String fax,
            @RequestParam(name = "zip", required = false) String zipCode,
            @RequestParam(name = "address_main", required = false) String baseAddress,
            @RequestParam(name = "address_detail", required = false) String detailAddress,
            RedirectAttributes redirectAttributes) {
        try {
            usersService.registerSeller(id, password, passwordConfirm, company, representative, licenseNumber, reportNumber, phone, fax, zipCode, baseAddress, detailAddress);
            redirectAttributes.addAttribute("id", id.trim());
            redirectAttributes.addAttribute("type", "seller");
            return "redirect:/member/welcome";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute("joinError", e.getMessage());
            return "redirect:/member/registerseller";
        }
    }

    @PostMapping("/member/find/userid")
    public String findUserId(
            @RequestParam String mode,
            @RequestParam String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            HttpSession session,
            Model model) {
        try {
            UsersService.FoundAccount account;
            if ("phone".equalsIgnoreCase(mode)) {
                account = usersService.findUserIdByPhone(name, phone)
                        .orElseThrow(() -> new IllegalArgumentException("입력한 정보와 일치하는 아이디가 없습니다."));
            } else {
                if (!isEmailVerified(email, session)) {
                    throw new IllegalArgumentException("이메일 인증을 완료해주세요.");
                }
                account = usersService.findUserIdByEmail(name, email)
                        .orElseThrow(() -> new IllegalArgumentException("입력한 정보와 일치하는 아이디가 없습니다."));
            }
            model.addAttribute("foundAccount", account);
            clearEmailVerification(session);
            return "member/find/resultid";
        } catch (IllegalArgumentException e) {
            model.addAttribute("findError", e.getMessage());
            model.addAttribute("selectedMode", "phone".equalsIgnoreCase(mode) ? "phone" : "email");
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            return "member/find/userid";
        }
    }

    @PostMapping("/member/find/password")
    public String findPassword(
            @RequestParam String mode,
            @RequestParam String id,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            HttpSession session,
            Model model) {
        try {
            if ("phone".equalsIgnoreCase(mode)) {
                usersService.findPasswordResetTargetByPhone(id, phone)
                        .orElseThrow(() -> new IllegalArgumentException("입력한 정보와 일치하는 계정이 없습니다."));
            } else {
                if (!isEmailVerified(email, session)) {
                    throw new IllegalArgumentException("이메일 인증을 완료해주세요.");
                }
                usersService.findPasswordResetTargetByEmail(id, email)
                        .orElseThrow(() -> new IllegalArgumentException("입력한 정보와 일치하는 계정이 없습니다."));
            }
            session.setAttribute(PASSWORD_RESET_ID_SESSION_KEY, id.trim());
            clearEmailVerification(session);
            model.addAttribute("resetReady", true);
            model.addAttribute("resetTargetId", id.trim());
            model.addAttribute("selectedMode", "phone".equalsIgnoreCase(mode) ? "phone" : "email");
            model.addAttribute("findMessage", "계정이 확인되었습니다. 새 비밀번호를 입력해주세요.");
            return "member/find/password";
        } catch (IllegalArgumentException e) {
            model.addAttribute("findError", e.getMessage());
            model.addAttribute("selectedMode", "phone".equalsIgnoreCase(mode) ? "phone" : "email");
            model.addAttribute("id", id);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            return "member/find/password";
        }
    }

    @PostMapping("/member/find/password/reset")
    public String resetPassword(
            @RequestParam String id,
            @RequestParam String password,
            @RequestParam String passwordConfirm,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        String resetId = (String) session.getAttribute(PASSWORD_RESET_ID_SESSION_KEY);
        if (resetId == null || !resetId.equals(id == null ? "" : id.trim())) {
            model.addAttribute("findError", "비밀번호를 변경할 아이디를 먼저 확인해주세요.");
            return "member/find/password";
        }
        try {
            usersService.resetPassword(resetId, password, passwordConfirm);
            session.removeAttribute(PASSWORD_RESET_ID_SESSION_KEY);
            redirectAttributes.addAttribute("loginMessage", "비밀번호가 변경되었습니다. 새 비밀번호로 로그인해주세요.");
            return "redirect:/member/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("findError", e.getMessage());
            model.addAttribute("resetReady", true);
            model.addAttribute("resetTargetId", resetId);
            return "member/find/password";
        }
    }

    @PostMapping("/member/find/changePassword")
    public String changeLoggedInPassword(
            Authentication authentication,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String passwordConfirm,
            Model model,
            RedirectAttributes redirectAttributes) {
        String loginId = authentication.getName();
        try {
            usersService.changePassword(loginId, currentPassword, newPassword, passwordConfirm);
            redirectAttributes.addAttribute("changed", "success");
            return "redirect:/member/find/changePassword";
        } catch (IllegalArgumentException e) {
            model.addAttribute("loginId", loginId);
            model.addAttribute("changePasswordError", e.getMessage());
            return "member/find/changePassword";
        }
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(String.valueOf(authentication.getPrincipal()));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private boolean hasClientRegistration(String registrationId) {
        ClientRegistrationRepository repository = clientRegistrationRepository.getIfAvailable();
        return repository != null && repository.findByRegistrationId(registrationId) != null;
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
