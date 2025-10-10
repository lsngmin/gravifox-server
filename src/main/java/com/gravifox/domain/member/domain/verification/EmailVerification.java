package com.gravifox.domain.member.domain.verification;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "email_verification", schema = "member")
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_verification_no", nullable = false, unique = true, updatable = false)
    private Long emailVerificationNo;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 32)
    private VerificationPurpose purpose;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private VerificationStatus status = VerificationStatus.PENDING;

    @Builder.Default
    @Column(name = "resend_count", nullable = false)
    private Integer resendCount = 0;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void markVerified() {
        this.status = VerificationStatus.VERIFIED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markExpired() {
        this.status = VerificationStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementResendCount() {
        this.resendCount = this.resendCount + 1;
        this.updatedAt = LocalDateTime.now();
    }
}

