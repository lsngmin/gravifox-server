package com.gravifox.domain.member.dto.mypage;

public record PasswordChangeRequest(
        String currentPassword,
        String newPassword
) {}
