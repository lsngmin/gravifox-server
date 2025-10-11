package com.gravifox.domain.file.token;

import com.gravifox.domain.file.exception.UploadTokenConflictException;
import com.gravifox.domain.file.exception.UploadTokenUnauthorizedException;
import com.gravifox.security.jwt.service.JwtService;
import com.gravifox.security.jwt.service.UploadTokenClaims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadTokenService {

    private final UploadTokenRepository uploadTokenRepository;
    private final JwtService jwtService;
    private final Clock clock;

    @Value("${jwt.upload.token-ttl-seconds:300}")
    private long defaultTtlSeconds;

    @Transactional
    public UploadTokenIssueResult issueToken(String uploadId) {
        if (uploadId == null || uploadId.isBlank()) {
            throw new IllegalArgumentException("uploadId must not be blank");
        }

        Duration ttl = Duration.ofSeconds(defaultTtlSeconds);
        Instant nowInstant = clock.instant();
        LocalDateTime now = LocalDateTime.ofInstant(nowInstant, clock.getZone());
        Instant expiresAtInstant = nowInstant.plus(ttl);
        LocalDateTime expiresAt = LocalDateTime.ofInstant(expiresAtInstant, clock.getZone());
        String jti = UUID.randomUUID().toString();

        revokeActiveTokens(uploadId, "새 토큰 발급");

        UploadToken entity = UploadToken.issue(uploadId, jti, now, expiresAt);
        uploadTokenRepository.save(entity);

        String token = jwtService.issueUploadToken(uploadId, jti, ttl, nowInstant);

        return new UploadTokenIssueResult(token, expiresAtInstant, uploadId, jti);
    }

    @Transactional
    public UploadAuthorizationContext beginConsumption(String token) {
        UploadTokenClaims claims;
        try {
            claims = jwtService.parseUploadToken(token);
        } catch (RuntimeException e) {
            throw new UploadTokenUnauthorizedException("업로드 토큰 검증에 실패했어요.");
        }

        UploadToken entity = uploadTokenRepository.findByJtiForUpdate(claims.jti())
                .orElseThrow(() -> new UploadTokenUnauthorizedException("등록된 업로드 토큰이 아니에요."));

        if (!entity.getUploadId().equals(claims.uploadId())) {
            entity.markFailed(clock, "uploadId mismatch");
            log.warn("Upload token uploadId mismatch: expected={}, actual={}", entity.getUploadId(), claims.uploadId());
            throw new UploadTokenUnauthorizedException("업로드 토큰 정보가 일치하지 않아요.");
        }

        if (entity.isExpired(clock)) {
            entity.markFailed(clock, "expired");
            throw new UploadTokenUnauthorizedException("업로드 토큰이 만료됐어요.");
        }

        if (claims.expiresAt().isBefore(clock.instant())) {
            entity.markFailed(clock, "jwt expired");
            throw new UploadTokenUnauthorizedException("업로드 토큰이 만료됐어요.");
        }

        if (entity.getStatus() == UploadTokenStatus.CONSUMED || entity.getStatus() == UploadTokenStatus.FAILED) {
            throw new UploadTokenConflictException("이미 처리된 업로드 토큰이에요.");
        }

        if (entity.getStatus() != UploadTokenStatus.ISSUED) {
            throw new UploadTokenUnauthorizedException("사용할 수 없는 업로드 토큰 상태예요.");
        }

        entity.markInProgress(clock);
        return new UploadAuthorizationContext(entity.getId(), entity.getJti(), entity.getUploadId(), claims.expiresAt());
    }

    @Transactional
    public void completeSuccess(UploadAuthorizationContext context) {
        if (context == null) {
            return;
        }
        uploadTokenRepository.findByIdForUpdate(context.tokenId()).ifPresent(entity -> {
            if (entity.getStatus() == UploadTokenStatus.CONSUMED) {
                return;
            }
            if (entity.getStatus() == UploadTokenStatus.IN_PROGRESS || entity.getStatus() == UploadTokenStatus.ISSUED) {
                entity.markConsumed(clock);
                log.debug("Upload token {} consumed successfully", entity.getJti());
            }
        });
    }

    @Transactional
    public void completeFailure(UploadAuthorizationContext context, String reason) {
        if (context == null) {
            return;
        }
        uploadTokenRepository.findByIdForUpdate(context.tokenId()).ifPresent(entity -> {
            String safeReason = (reason == null || reason.isBlank()) ? "unknown" : reason;
            if (entity.getStatus() != UploadTokenStatus.CONSUMED) {
                entity.markFailed(clock, safeReason);
                log.debug("Upload token {} marked as failed: {}", entity.getJti(), safeReason);
            }
        });
    }

    public List<Map<String, Object>> jwks() {
        return jwtService.jwks();
    }

    private void revokeActiveTokens(String uploadId, String reason) {
        List<UploadToken> actives = uploadTokenRepository.findAllByUploadIdAndStatusIn(
                uploadId, List.of(UploadTokenStatus.ISSUED, UploadTokenStatus.IN_PROGRESS)
        );
        actives.forEach(token -> token.revoke(clock, reason));
    }

    public record UploadTokenIssueResult(String token, Instant expiresAt, String uploadId, String jti) {}
}
