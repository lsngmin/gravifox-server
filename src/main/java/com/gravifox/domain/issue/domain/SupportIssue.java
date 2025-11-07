package com.gravifox.domain.issue.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "support_issue")
@Getter
@Setter
@NoArgsConstructor
public class SupportIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_no")
    private Long userNo;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "category", length = 64)
    private String category; // bug-report | feature-request | support-contact

    @Column(name = "title", length = 500)
    private String title;

    @Lob
    @Column(name = "body")
    private String body;

    @Column(name = "state", length = 32)
    private String state; // open | closed

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (state == null || state.isBlank()) {
            state = "open";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

