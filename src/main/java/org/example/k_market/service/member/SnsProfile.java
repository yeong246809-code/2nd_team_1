package org.example.k_market.service.member;

public record SnsProfile(
        SnsProvider provider,
        String providerId,
        String email,
        String name,
        String phone
) {
    public SnsProfile {
        if (provider == null) {
            throw new IllegalArgumentException("SNS 제공자 정보가 없습니다.");
        }
        if (providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException("SNS 식별자 정보가 없습니다.");
        }
        providerId = providerId.trim();
        email = trimToNull(email);
        name = trimToNull(name);
        phone = trimToNull(phone);
    }

    public String displayName() {
        return name != null ? name : "SNS 사용자";
    }

    private static String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
