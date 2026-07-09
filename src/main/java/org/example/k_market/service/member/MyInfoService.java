package org.example.k_market.service.member;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.MyInfoUpdateDTO;
import org.example.k_market.entity.Member;
import org.example.k_market.entity.Shop;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.MemberRepository;
import org.example.k_market.repository.ShopRepository;
import org.example.k_market.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyInfoService {

    private final UsersRepository usersRepository;
    private final MemberRepository memberRepository;
    private final ShopRepository shopRepository;

    @Transactional
    public void update(String loginId, MyInfoUpdateDTO dto) {
        Users user = usersRepository.findById(loginId)
                .orElseThrow(() -> new IllegalArgumentException("로그인 정보를 찾을 수 없습니다."));

        if ("SELLER".equalsIgnoreCase(user.getRole())) {
            updateSeller(user.getMemberNo(), dto);
            return;
        }

        updateMember(user.getMemberNo(), dto);
    }

    private void updateMember(int memberNo, MyInfoUpdateDTO dto) {
        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        String emailLocal = required(dto.getEmailLocal(), "이메일 아이디를 입력해주세요.");
        String emailDomain = required(dto.getEmailDomain(), "이메일 도메인을 입력해주세요.");
        String email = emailLocal + "@" + emailDomain;
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("이메일 주소를 정확히 입력해주세요.");
        }

        String phone = digits(dto.getPhoneFirst())
                + digits(dto.getPhoneMiddle())
                + digits(dto.getPhoneLast());
        if (phone.length() < 10 || phone.length() > 11) {
            throw new IllegalArgumentException("휴대폰 번호를 정확히 입력해주세요.");
        }

        Member updatedMember = member.toBuilder()
                .name(required(dto.getName(), "이름을 입력해주세요."))
                .email(email)
                .phone(phone)
                .zipCode(trimToNull(dto.getZipCode()))
                .baseAddress(valueOrEmpty(dto.getBaseAddress()))
                .detailAddress(trimToNull(dto.getDetailAddress()))
                .build();

        memberRepository.save(updatedMember);
    }

    private void updateSeller(int memberNo, MyInfoUpdateDTO dto) {
        Shop shop = shopRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("판매자 상점 정보를 찾을 수 없습니다."));

        shop.updateProfile(
                required(dto.getShopName(), "상호명을 입력해주세요."),
                required(dto.getCeo(), "대표자명을 입력해주세요."),
                trimToNull(dto.getMailOrderNumber()),
                trimToNull(dto.getShopPhone()),
                trimToNull(dto.getFax()),
                trimToNull(dto.getShopZipCode()),
                valueOrEmpty(dto.getShopBaseAddress()),
                valueOrEmpty(dto.getShopDetailAddress()));
    }

    private String required(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new IllegalArgumentException(message);
        }
        return trimmed;
    }

    private String digits(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String valueOrEmpty(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? "" : trimmed;
    }
}
