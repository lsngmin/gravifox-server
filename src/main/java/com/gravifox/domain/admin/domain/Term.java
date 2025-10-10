package com.gravifox.domain.admin.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_term_meta", schema = "member")
public class Term {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "term_id", nullable = false, unique = true, updatable = false)
    private Long termId;

    @Column(name = "term_key", nullable = false, length = 50)
    private String termKey;

    @Column(name = "term_title", nullable = false, length = 100)
    private String termTitle;

    @Column(name = "term_version", nullable = false, length = 20)
    private String termVersion;

    @Lob
    @Column(name = "term_content", nullable = false, columnDefinition = "TEXT")
    private String termContent;

    @Builder.Default
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = true;

    @Builder.Default
    @Column(name = "effective_at", nullable = false)
    private LocalDateTime effectiveAt = LocalDateTime.now();

    @Column(name = "deprecated_at")
    private LocalDateTime deprecatedAt;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
