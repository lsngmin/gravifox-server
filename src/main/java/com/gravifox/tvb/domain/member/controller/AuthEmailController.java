package com.gravifox.tvb.domain.member.controller;

import com.gravifox.tvb.annotation.LogContext;
import com.gravifox.tvb.domain.member.domain.verification.VerificationPurpose;
import com.gravifox.tvb.domain.member.dto.verification.EmailVerificationRequest;
import com.gravifox.tvb.domain.member.dto.verification.EmailVerificationResponse;
import com.gravifox.tvb.domain.member.exception.auth.ExpiredVerificationTokenException;
import com.gravifox.tvb.domain.member.exception.auth.InvalidVerificationTokenException;
import com.gravifox.tvb.domain.member.service.AuthService;
import com.gravifox.tvb.domain.member.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/email")
@Tag(name = "이메일 인증", description = "이메일 인증 링크 요청/검증 API")
public class AuthEmailController {

    private final EmailVerificationService emailVerificationService;
    private final AuthService authService;

    @Value("${front.redirect.url}")
    private String frontRedirectUrl;

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;
    @Value("${app.cookie.same-site:None}")
    private String cookieSameSite;
    @Value("${app.cookie.max-age-days:7}")
    private int cookieMaxAgeDays;
    @Value("${app.cookie.domain:}")
    private String cookieDomain;

    @GetMapping("/verify")
    @Operation(summary = "이메일 인증 링크 검증")
    public ResponseEntity<Void> verify(@RequestParam("token") String token, HttpServletResponse response) {
        try {
            // 1) 토큰 검증 및 사용자 이메일 획득 + 인증 처리
            String email = emailVerificationService.verifyToken(token);

            // 2) 인증 완료된 사용자에 대해 토큰 발급(Refresh 쿠키 저장)
            Map<String, String> tokens = authService.issueTokensForUserId(email);
            response.addCookie(authService.storeRefreshTokenInCookie(tokens.get("refreshToken")));

            // 3) 게스트 쿠키 제거
            Cookie guestCookie = new Cookie("guest", null);
            guestCookie.setHttpOnly(false);
            guestCookie.setSecure(cookieSecure);
            guestCookie.setPath("/");
            guestCookie.setMaxAge(0);
            guestCookie.setAttribute("SameSite", cookieSameSite);
            if (cookieDomain != null && !cookieDomain.isBlank()) {
                guestCookie.setDomain(cookieDomain);
            }
            response.addCookie(guestCookie);

            // 4) 메인으로 리다이렉트 (로그인 유지 상태: refresh 쿠키 기반)
            return ResponseEntity.status(302)
                    .header("Location", frontRedirectUrl)
                    .build();
        } catch (ExpiredVerificationTokenException | InvalidVerificationTokenException e) {
            // 이미 사용/만료된 토큰이어도 프론트엔드 메인으로 리다이렉트
            return ResponseEntity.status(302)
                    .header("Location", frontRedirectUrl)
                    .build();
        }
    }

    @PostMapping("/request")
    @Operation(summary = "이메일 인증 메일 요청/재발송")
    public ResponseEntity<EmailVerificationResponse> request(@RequestBody EmailVerificationRequest request) {
        VerificationPurpose purpose = VerificationPurpose.fromString(request.getPurpose());
        EmailVerificationResponse res = emailVerificationService.requestVerification(request.getEmail(), purpose);
        return ResponseEntity.ok(res);
    }
}
