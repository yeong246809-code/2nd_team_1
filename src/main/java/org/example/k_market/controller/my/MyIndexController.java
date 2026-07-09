package org.example.k_market.controller.my;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.dto.MyInfoUpdateDTO;
import org.example.k_market.entity.Member;
import org.example.k_market.entity.Shop;
import org.example.k_market.entity.SnsAccount;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.MemberRepository;
import org.example.k_market.repository.ShopRepository;
import org.example.k_market.repository.SnsAccountRepository;
import org.example.k_market.repository.UsersRepository;
import org.example.k_market.service.member.MemberWithdrawalService;
import org.example.k_market.service.member.MyInfoService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@Log4j2
@RequiredArgsConstructor
public class MyIndexController {

    private static final DateTimeFormatter BIRTH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

    private final UsersRepository usersRepository;
    private final MemberRepository memberRepository;
    private final ShopRepository shopRepository;
    private final SnsAccountRepository snsAccountRepository;
    private final MemberWithdrawalService memberWithdrawalService;
    private final MyInfoService myInfoService;

    @GetMapping("/my/index")
    public String index(Authentication authentication, HttpSession session, Model model) {
        addLoginModel(authentication, session, model);
        return "my/index";
    }

    @GetMapping("/my/order")
    public String order(Authentication authentication, HttpSession session, Model model) {
        addLoginModel(authentication, session, model);
        return "my/order";
    }

    @GetMapping("/my/point")
    public String point(Authentication authentication, HttpSession session, Model model) {
        addLoginModel(authentication, session, model);
        return "my/point";
    }

    @GetMapping("/my/coupon")
    public String coupon(Authentication authentication, HttpSession session, Model model) {
        addLoginModel(authentication, session, model);
        return "my/coupon";
    }

    @GetMapping("/my/review")
    public String review(Authentication authentication, HttpSession session, Model model) {
        addLoginModel(authentication, session, model);
        return "my/review";
    }

    @GetMapping("/my/myqna")
    public String myqna(Authentication authentication, HttpSession session, Model model) {
        addLoginModel(authentication, session, model);
        return "my/myqna";
    }

    @GetMapping("/my/myinfo")
    public String myinfo(Authentication authentication, HttpSession session, Model model) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/member/login";
        }

        addLoginModel(authentication, session, model);
        addMyInfoModel(authentication, model);
        return "my/myinfo";
    }

    @PostMapping("/my/myinfo")
    public String updateMyInfo(
            Authentication authentication,
            MyInfoUpdateDTO dto,
            RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/member/login";
        }

        try {
            myInfoService.update(authentication.getName(), dto);
            redirectAttributes.addFlashAttribute("myInfoMessage", "회원 정보가 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("myInfoError", e.getMessage());
        }

        return "redirect:/my/myinfo";
    }

    @PostMapping("/my/withdraw")
    public String withdraw(
            Authentication authentication,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/member/login";
        }

        try {
            memberWithdrawalService.withdraw(authentication.getName());
            SecurityContextHolder.clearContext();
            session.invalidate();
            redirectAttributes.addAttribute("withdrawn", "success");
            return "redirect:/member/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute("withdrawError", e.getMessage());
            return "redirect:/my/myinfo";
        }
    }

    private void addLoginModel(Authentication authentication, HttpSession session, Model model) {
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

        model.addAttribute("loginUserId", authenticated ? sessUser : "");
        model.addAttribute("loginUserRole", role.isBlank() ? "USER" : role);
        model.addAttribute("isAdmin", "ADMIN".equalsIgnoreCase(role));
    }

    private void addMyInfoModel(Authentication authentication, Model model) {
        Optional<Users> user = currentUser(authentication);
        Optional<Member> member = user.flatMap(value -> memberRepository.findById(value.getMemberNo()));
        boolean seller = user
                .map(Users::getRole)
                .map(role -> "SELLER".equalsIgnoreCase(role))
                .orElse(false);
        Optional<Shop> shop = seller
                ? user.flatMap(value -> shopRepository.findById(value.getMemberNo()))
                : Optional.empty();

        String email = member.map(Member::getEmail).orElse("");
        String[] emailParts = splitEmail(email);
        String phone = member.map(Member::getPhone).orElse("");
        String[] phoneParts = splitPhone(phone);

        model.addAttribute("myInfoSeller", seller);
        model.addAttribute("myInfoUserId", user.map(Users::getId).orElse(""));
        model.addAttribute("myInfoName", member.map(Member::getName).orElse(""));
        model.addAttribute("myInfoBirthDate", formatBirthDate(member.map(Member::getBirthDate).orElse(null)));
        model.addAttribute("myInfoEmailLocal", emailParts[0]);
        model.addAttribute("myInfoEmailDomain", emailParts[1]);
        model.addAttribute("myInfoPhoneFirst", phoneParts[0]);
        model.addAttribute("myInfoPhoneMiddle", phoneParts[1]);
        model.addAttribute("myInfoPhoneLast", phoneParts[2]);
        model.addAttribute("myInfoZipCode", member.map(Member::getZipCode).orElse(""));
        model.addAttribute("myInfoBaseAddress", member.map(Member::getBaseAddress).orElse(""));
        model.addAttribute("myInfoDetailAddress", member.map(Member::getDetailAddress).orElse(""));

        model.addAttribute("myInfoShopName", shop.map(Shop::getName).orElse(""));
        model.addAttribute("myInfoCeo", shop.map(Shop::getCeo).orElse(""));
        model.addAttribute("myInfoBizNumber", shop.map(Shop::getBizNumber).orElse(""));
        model.addAttribute("myInfoMailOrderNumber", shop.map(Shop::getMailOrderNumber).orElse(""));
        model.addAttribute("myInfoShopPhone", shop.map(Shop::getPhone).orElse(""));
        model.addAttribute("myInfoFax", shop.map(Shop::getFax).orElse(""));
        model.addAttribute("myInfoShopZipCode", shop.map(Shop::getZipCode).orElse(""));
        model.addAttribute("myInfoShopBaseAddress", shop.map(Shop::getBaseAddress).orElse(""));
        model.addAttribute("myInfoShopDetailAddress", shop.map(Shop::getDetailAddress).orElse(""));

        int memberNo = user.map(Users::getMemberNo).orElse(0);
        List<SnsAccount> snsAccounts = memberNo == 0 ? List.of() : snsAccountRepository.findAllByMemberNo(memberNo);
        model.addAttribute("naverConnected", hasProvider(snsAccounts, "naver"));
        model.addAttribute("kakaoConnected", hasProvider(snsAccounts, "kakao"));
    }

    private Optional<Users> currentUser(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return Optional.empty();
        }
        return usersRepository.findById(authentication.getName());
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(String.valueOf(authentication.getPrincipal()));
    }

    private String[] splitEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return new String[]{"", ""};
        }
        String[] parts = email.split("@", 2);
        return new String[]{parts[0], parts.length > 1 ? parts[1] : ""};
    }

    private String[] splitPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return new String[]{"010", "", ""};
        }
        String normalized = phone.replaceAll("[^0-9]", "");
        if (normalized.length() >= 10) {
            String first = normalized.startsWith("02") ? "02" : normalized.substring(0, 3);
            int middleStart = first.length();
            int lastStart = normalized.length() - 4;
            return new String[]{
                    first,
                    normalized.substring(middleStart, lastStart),
                    normalized.substring(lastStart)
            };
        }
        return new String[]{"010", "", ""};
    }

    private String formatBirthDate(LocalDate birthDate) {
        if (birthDate == null || LocalDate.of(1900, 1, 1).equals(birthDate)) {
            return "-";
        }
        return birthDate.format(BIRTH_DATE_FORMATTER);
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private boolean hasProvider(List<SnsAccount> snsAccounts, String provider) {
        return snsAccounts.stream()
                .anyMatch(account -> provider.equalsIgnoreCase(account.getProvider()));
    }
}
