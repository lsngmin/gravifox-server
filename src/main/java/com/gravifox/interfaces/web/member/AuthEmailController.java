package com.gravifox.interfaces.web.member;

import com.gravifox.domain.member.domain.verification.VerificationPurpose;
import com.gravifox.domain.member.dto.verification.EmailVerificationRequest;
import com.gravifox.domain.member.dto.verification.EmailVerificationResponse;
import com.gravifox.domain.member.exception.auth.ExpiredVerificationTokenException;
import com.gravifox.domain.member.exception.auth.InvalidVerificationTokenException;
import com.gravifox.domain.member.service.AuthService;
import com.gravifox.domain.member.service.EmailVerificationService;
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

    @Value("${front.redirect.url:/}")
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
            String email = emailVerificationService.verifyToken(token);
            Map<String, String> tokens = authService.issueTokensForUserId(email);
            response.addCookie(authService.storeRefreshTokenInCookie(tokens.get("refreshToken")));

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

            return ResponseEntity.status(302)
                    .header("Location", frontRedirectUrl)
                    .build();
        } catch (ExpiredVerificationTokenException | InvalidVerificationTokenException e) {
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
