package com.gravifox.tvb.domain.member.domain.user;

import com.gravifox.tvb.domain.member.domain.Password;
import com.gravifox.tvb.domain.member.domain.Profile;
import com.gravifox.tvb.domain.member.domain.SocialLogin;
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
@Table(name = "user", schema = "member")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_no", nullable = false, unique = true, updatable = false)
    private Long userNo;

    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;

    @Convert(converter = LoginTypeConvertor.class)
    @Column(name = "login_type", nullable = false)
    private LoginType loginType;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Password password;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private SocialLogin socialLogin;
}
