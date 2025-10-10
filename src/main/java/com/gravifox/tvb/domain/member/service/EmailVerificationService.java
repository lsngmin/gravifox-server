package com.gravifox.tvb.domain.member.service;

import com.gravifox.tvb.domain.member.domain.verification.VerificationPurpose;
import com.gravifox.tvb.domain.member.dto.verification.EmailVerificationResponse;

public interface EmailVerificationService {
    /**
     * 주어진 토큰을 검증하고 해당 이메일을 인증 처리합니다.
     *
     * @param token 이메일 인증 토큰
     * @return 인증된 사용자의 이메일(정규화된 소문자)
     */
    String verifyToken(String token);

    EmailVerificationResponse requestVerification(String email, VerificationPurpose purpose);
}
