package org.example.k_market.service.member;

import java.util.Locale;

public enum MemberAccountStatus {

    ACTIVE("정상"),
    STOPPED("중지"),
    INACTIVE("휴면"),
    WITHDRAWN("탈퇴");

    private final String label;

    MemberAccountStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean canLogin() {
        return this == ACTIVE || this == INACTIVE;
    }

    public static MemberAccountStatus from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "ACTIVE", "정상" -> ACTIVE;
            case "STOPPED", "SUSPENDED", "BLOCKED", "중지" -> STOPPED;
            case "INACTIVE", "DORMANT", "휴면", "휴먼" -> INACTIVE;
            case "WITHDRAWN", "DELETED", "탈퇴" -> WITHDRAWN;
            default -> throw new IllegalArgumentException("지원하지 않는 회원 상태입니다: " + value);
        };
    }

    public static boolean isWithdrawn(MemberAccountStatus status) {
        return status == WITHDRAWN;
    }

    public static boolean isBlockedForLogin(MemberAccountStatus status) {
        return status == null || !status.canLogin();
    }

    public static String loginBlockMessage(MemberAccountStatus status) {
        if (status == WITHDRAWN) {
            return "탈퇴한 아이디라서 로그인할 수 없습니다.";
        }
        if (status == STOPPED) {
            return "중지된 계정입니다. 고객센터에 문의해주세요.";
        }
        if (status == INACTIVE) {
            return "휴면 계정입니다. 고객센터에 문의해주세요.";
        }
        return "로그인할 수 없는 계정 상태입니다. 고객센터에 문의해주세요.";
    }
}
