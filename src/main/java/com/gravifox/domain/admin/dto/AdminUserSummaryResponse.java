package com.gravifox.domain.admin.dto;

import java.time.LocalDateTime;

public record AdminUserSummaryResponse(
        Long userNo,
        String userId,
        String nickname,
        String loginType,
        boolean emailVerified,
        LocalDateTime createdAt,
        LocalDateTime lastAnalysisAt,
        Integer monthlyQuotaLimit,
        Integer monthlyQuotaUsed,
        Integer monthlyQuotaRemaining
) {

    public static AdminUserSummaryResponse from(AdminUserSummaryQueryResult result, int defaultMonthlyLimit) {
        int limit = result.monthlyQuotaLimit() != null ? result.monthlyQuotaLimit() : defaultMonthlyLimit;
        int used = Math.max(0, result.monthlyQuotaUsed() != null ? result.monthlyQuotaUsed() : 0);
        int remaining = Math.max(0, limit - used);

        return new AdminUserSummaryResponse(
                result.userNo(),
                result.userId(),
                result.nickname(),
                result.loginType() != null ? result.loginType().name() : null,
                Boolean.TRUE.equals(result.emailVerified()),
                result.createdAt(),
                result.lastAnalysisAt(),
                limit,
                used,
                remaining
        );
    }
}
