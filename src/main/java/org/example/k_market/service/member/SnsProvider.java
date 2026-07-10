package org.example.k_market.service.member;

import java.util.Arrays;

public enum SnsProvider {
    NAVER("naver", "@n"),
    KAKAO("kakao", "@k"),
    GOOGLE("google", "@g");

    private final String registrationId;
    private final String idSuffix;

    SnsProvider(String registrationId, String idSuffix) {
        this.registrationId = registrationId;
        this.idSuffix = idSuffix;
    }

    public String registrationId() {
        return registrationId;
    }

    public String idSuffix() {
        return idSuffix;
    }

    public static SnsProvider fromRegistrationId(String registrationId) {
        return Arrays.stream(values())
                .filter(provider -> provider.registrationId.equalsIgnoreCase(registrationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 SNS 제공자입니다: " + registrationId));
    }
}
