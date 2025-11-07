package com.gravifox.domain.member.service.impl;

import com.gravifox.domain.member.domain.verification.EmailVerification;
import com.gravifox.domain.member.domain.verification.VerificationPurpose;
import com.gravifox.domain.member.domain.verification.VerificationStatus;
import com.gravifox.domain.member.dto.verification.EmailVerificationResponse;
import com.gravifox.domain.member.exception.auth.ExpiredVerificationTokenException;
import com.gravifox.domain.member.exception.auth.InvalidVerificationTokenException;
import com.gravifox.domain.member.repository.EmailVerificationRepository;
import com.gravifox.domain.member.repository.UserRepository;
import com.gravifox.domain.member.service.EmailVerificationService;
import com.gravifox.domain.notification.MailSender;
import com.gravifox.domain.notification.template.VerificationEmailTemplate;
import com.gravifox.util.HashUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final MailSender mailSender;

    @Value("${email.verification.ttl-seconds:86400}")
    private long ttlSeconds;

    @Value("${email.verification.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    @Value("${email.verification.verify-base-url:http://localhost:8080/api/v1/auth/email/verify}")
    private String verifyBaseUrl;

    @Value("${email.verification.service-name:GraviFox}")
    private String serviceName;

    @Value("${email.verification.subject:[Email Verification] 이메일 인증을 완료해주세요}")
    private String verificationSubject;

    @Value("${email.verification.rate-limit-per-hour:0}")
    private int rateLimitPerHour;

    @Override
    @Transactional
    public String verifyToken(String token) {
        String tokenHash = HashUtil.sha256Hex(token);

        EmailVerification ev = emailVerificationRepository
                .findByTokenHashAndStatus(tokenHash, VerificationStatus.PENDING)
                .orElseThrow(InvalidVerificationTokenException::new);

        if (ev.getExpiresAt() != null && ev.getExpiresAt().isBefore(LocalDateTime.now())) {
            ev.markExpired();
            emailVerificationRepository.save(ev);
            throw new ExpiredVerificationTokenException();
        }

        ev.markVerified();
        emailVerificationRepository.save(ev);

        userRepository.findByUserId(ev.getEmail()).ifPresent(user -> {
            user.verifyEmail();
            userRepository.save(user);
        });
        return ev.getEmail();
    }

    @Override
    @Transactional
    public EmailVerificationResponse requestVerification(String email, VerificationPurpose purpose) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            // 단순화: 요청은 항상 200을 반환하고, 내부에서만 유효성 체크
            return new EmailVerificationResponse(false, resendCooldownSeconds);
        }

        LocalDateTime now = LocalDateTime.now();

        // 시간당 레이트리밋(옵션). 설정값이 1 이상일 때만 적용
        if (rateLimitPerHour > 0) {
            long cnt = emailVerificationRepository.countByEmailAndCreatedAtAfter(normalizedEmail, now.minusHours(1));
            if (cnt >= rateLimitPerHour) {
                long secondsSinceHour = java.time.Duration.between(now.withMinute(0).withSecond(0).withNano(0), now).getSeconds();
                long remainToNextHour = Math.max(1, 3600 - secondsSinceHour);
                return new EmailVerificationResponse(false, remainToNextHour);
            }
        }
        // 최근 PENDING 레코드 조회
        var latestPending = emailVerificationRepository
                .findTopByEmailAndPurposeAndStatusOrderByCreatedAtDesc(normalizedEmail, purpose, VerificationStatus.PENDING);

        if (latestPending.isPresent()) {
            EmailVerification ev = latestPending.get();
            long secondsSinceCreated = java.time.Duration.between(ev.getCreatedAt(), now).getSeconds();
            long remain = resendCooldownSeconds - secondsSinceCreated;
            if (remain > 0) {
                // 쿨다운 미종료: 실제 발송 없이 남은 시간 안내
                return new EmailVerificationResponse(false, remain);
            }
        }

        // 새 토큰 생성 및 저장
        String token = generateToken();
        String tokenHash = HashUtil.sha256Hex(token);
        EmailVerification newEv = EmailVerification.builder()
                .email(normalizedEmail)
                .purpose(purpose)
                .tokenHash(tokenHash)
                .expiresAt(now.plusSeconds(ttlSeconds))
                .build();
        emailVerificationRepository.save(newEv);

        // 링크/메일 발송
        String link = verifyBaseUrl + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        String subject = verificationSubject;
        String html = VerificationEmailTemplate.render(serviceName, link);
        mailSender.send(normalizedEmail, subject, html);

        return new EmailVerificationResponse(true, resendCooldownSeconds);
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        // URL-safe base64 without padding
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
