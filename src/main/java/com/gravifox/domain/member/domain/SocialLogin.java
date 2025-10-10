package com.gravifox.domain.member.domain;


import com.gravifox.domain.member.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "social_login", schema = "member")
public class SocialLogin {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "social_no")
    private Long socialNo;

    @Column(name = "providerId")
    private String providerId;

    @Column(name = "social_id")
    private String socialId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no", referencedColumnName = "user_no", nullable = false)
    private User user;


}
