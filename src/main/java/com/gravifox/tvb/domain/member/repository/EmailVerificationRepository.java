package com.gravifox.tvb.domain.member.repository;

import com.gravifox.tvb.domain.member.domain.verification.EmailVerification;
import com.gravifox.tvb.domain.member.domain.verification.VerificationPurpose;
import com.gravifox.tvb.domain.member.domain.verification.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByTokenHashAndStatus(String tokenHash, VerificationStatus status);

    Optional<EmailVerification> findTopByEmailAndPurposeAndStatusOrderByCreatedAtDesc(
            String email,
            VerificationPurpose purpose,
            VerificationStatus status
    );

    long countByEmailAndCreatedAtAfter(String email, LocalDateTime after);
}

