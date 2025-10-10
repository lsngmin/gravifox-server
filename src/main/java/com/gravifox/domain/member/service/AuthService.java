package com.gravifox.domain.member.service;

import com.gravifox.domain.member.dto.login.LoginRequest;
import jakarta.servlet.http.Cookie;

import java.util.Map;

public interface AuthService {
    Map<String, String> makeTokenAndLogin(LoginRequest loginRequest);
    Map<String, String> RefreshToken(String accessToken, String refreshToken);
    Map<String, Object> validateUserToken(String accessToken);

    Cookie storeRefreshTokenInCookie(String cValue);

    /**
     * 비밀번호 입력 없이 지정한 사용자에 대해 토큰을 발급합니다.
     * 주로 이메일 인증 완료 이후 자동 로그인에 사용됩니다.
     *
     * @param userId 사용자 이메일(ID)
     * @return accessToken, refreshToken, userId를 포함한 맵
     */
    Map<String, String> issueTokensForUserId(String userId);
}
