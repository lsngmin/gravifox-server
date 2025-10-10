package com.gravifox.domain.member.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_user_terms")
public class UserTerm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long utid;

    @Column(name = "user_no", nullable = false)
    private Long userNo;

    @Column(name = "term_id", nullable = false)
    private Long termId;

    @Column(name = "agreed_version", nullable = false, length = 20)
    private String agreedVersion;

    @Builder.Default
    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime agreedAt = LocalDateTime.now();

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
