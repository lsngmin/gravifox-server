package com.gravifox.domain.admin.dto;

import java.time.LocalDateTime;

public record AdminUserSummaryResponse(
        Long userNo,
        String userId,
        String nickname,
        String loginType,
        boolean emailVerified,
        LocalDateTime lastAnalysisAt,
        Integer monthlyQuotaLimit,
        Integer monthlyQuotaUsed,
        Integer monthlyQuotaRemaining
) {

    public static AdminUserSummaryResponse from(AdminUserSummaryQueryResult result) {
        Integer limit = result.monthlyQuotaLimit();
        Integer used = result.monthlyQuotaUsed();
        Integer remaining = null;
        if (limit != null && used != null) {
            remaining = limit - used;
            if (remaining < 0) {
                remaining = 0;
            }
        }

        return new AdminUserSummaryResponse(
                result.userNo(),
                result.userId(),
                result.nickname(),
                result.loginType() != null ? result.loginType().name() : null,
                Boolean.TRUE.equals(result.emailVerified()),
                result.lastAnalysisAt(),
                limit,
                used,
                remaining
        );
    }
}

