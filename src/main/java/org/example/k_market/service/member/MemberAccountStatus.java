package org.example.k_market.service.member;

import java.util.Locale;

public final class MemberAccountStatus {

    public static final String ACTIVE = "ACTIVE";
    public static final String WITHDRAWN = "탈퇴";
    public static final String SUSPENDED = "중지";
    public static final String DORMANT = "휴면";

    private MemberAccountStatus() {
    }

    public static boolean isWithdrawn(String status) {
        String normalized = normalize(status);
        return "탈퇴".equals(normalized)
                || "WITHDRAWN".equals(normalized)
                || "DELETED".equals(normalized);
    }

    public static boolean isSuspended(String status) {
        String normalized = normalize(status);
        return "중지".equals(normalized)
                || "SUSPENDED".equals(normalized)
                || "BLOCKED".equals(normalized)
                || "STOPPED".equals(normalized);
    }

    public static boolean isDormant(String status) {
        String normalized = normalize(status);
        return "휴면".equals(normalized)
                || "휴먼".equals(normalized)
                || "DORMANT".equals(normalized);
    }

    public static boolean isBlockedForLogin(String status) {
        return isWithdrawn(status) || isSuspended(status) || isDormant(status);
    }

    public static String loginBlockMessage(String status) {
        if (isWithdrawn(status)) {
            return "탈퇴한 아이디라서 로그인할 수 없습니다.";
        }
        if (isSuspended(status)) {
            return "중지된 계정입니다. 고객센터에 문의해주세요.";
        }
        if (isDormant(status)) {
            return "휴면 계정입니다. 고객센터에 문의해주세요.";
        }
        return "로그인할 수 없는 계정 상태입니다. 고객센터에 문의해주세요.";
    }

    private static String normalize(String status) {
        return status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
    }
}
