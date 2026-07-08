package org.example.k_market.service.member;

import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.Users;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final String LOGIN_ID_ATTRIBUTE = "loginId";

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final SnsLoginService snsLoginService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        SnsProvider provider = SnsProvider.fromRegistrationId(
                userRequest.getClientRegistration().getRegistrationId());
        SnsProfile profile = extractProfile(provider, oauth2User.getAttributes());
        Users user = snsLoginService.findOrCreateUser(profile);

        Map<String, Object> attributes = new LinkedHashMap<>(oauth2User.getAttributes());
        attributes.put(LOGIN_ID_ATTRIBUTE, user.getId());
        attributes.put("memberNo", user.getMemberNo());
        attributes.put("provider", provider.registrationId());

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                attributes,
                LOGIN_ID_ATTRIBUTE);
    }

    private SnsProfile extractProfile(SnsProvider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case NAVER -> extractNaverProfile(attributes);
            case KAKAO -> extractKakaoProfile(attributes);
        };
    }

    private SnsProfile extractNaverProfile(Map<String, Object> attributes) {
        Map<String, Object> response = getMap(attributes, "response");
        return new SnsProfile(
                SnsProvider.NAVER,
                getString(response, "id"),
                getString(response, "email"),
                firstNonBlank(getString(response, "name"), getString(response, "nickname")),
                getString(response, "mobile"));
    }

    private SnsProfile extractKakaoProfile(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = getMap(attributes, "kakao_account");
        Map<String, Object> profile = getMap(kakaoAccount, "profile");

        return new SnsProfile(
                SnsProvider.KAKAO,
                getString(attributes, "id"),
                getString(kakaoAccount, "email"),
                firstNonBlank(getString(kakaoAccount, "name"), getString(profile, "nickname")),
                getString(kakaoAccount, "phone_number"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private String getString(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }
}
