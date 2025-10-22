package com.gravifox.domain.admin.dto;

import com.gravifox.domain.member.domain.user.LoginType;

import java.time.LocalDateTime;

public record AdminUserSummaryQueryResult(
        Long userNo,
        String userId,
        String nickname,
        LoginType loginType,
        Boolean emailVerified,
        LocalDateTime lastAnalysisAt,
        Integer monthlyQuotaLimit,
        Integer monthlyQuotaUsed
) {
}

