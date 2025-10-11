package com.gravifox.domain.file.token;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "upload_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UploadToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String uploadId;

    @Column(nullable = false, length = 64, unique = true)
    private String jti;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UploadTokenStatus status;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime updatedAt;

    @Column(length = 255)
    private String failureReason;

    protected UploadToken(String uploadId, String jti, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        this.uploadId = uploadId;
        this.jti = jti;
        this.status = UploadTokenStatus.ISSUED;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.updatedAt = issuedAt;
    }

    public static UploadToken issue(String uploadId, String jti, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        return new UploadToken(uploadId, jti, issuedAt, expiresAt);
    }

    public boolean isExpired(Clock clock) {
        return expiresAt.isBefore(LocalDateTime.now(clock));
    }

    public void markInProgress(Clock clock) {
        this.status = UploadTokenStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now(clock);
        this.failureReason = null;
    }

    public void markConsumed(Clock clock) {
        this.status = UploadTokenStatus.CONSUMED;
        this.updatedAt = LocalDateTime.now(clock);
        this.failureReason = null;
    }

    public void markFailed(Clock clock, String reason) {
        this.status = UploadTokenStatus.FAILED;
        this.updatedAt = LocalDateTime.now(clock);
        this.failureReason = reason;
    }

    public void revoke(Clock clock, String reason) {
        this.status = UploadTokenStatus.REVOKED;
        this.updatedAt = LocalDateTime.now(clock);
        this.failureReason = reason;
    }

    public void touch(Clock clock) {
        this.updatedAt = LocalDateTime.now(clock);
    }
}
