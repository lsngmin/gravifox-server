package com.gravifox.tvb.domain.member.dto.verification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailVerificationResponse {
    private final boolean sent;
    private final long resendAfter; // seconds
}

