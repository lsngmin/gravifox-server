package com.tvb.api.domain.user.entity.auth;

import com.tvb.api.domain.user.entity.member.User;
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
@Table(name = "password", schema = "member")
public class Password {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "password_no", nullable = false, unique = true, updatable = false)
    private Long passwordNo;

    @Column(name = "password", nullable = false, unique = true)
    private String password;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no", referencedColumnName = "user_no", nullable = false)
    private User user;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
