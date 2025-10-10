package com.gravifox.tvb.domain.member.service.oauth2;

import com.gravifox.tvb.domain.member.domain.Password;
import com.gravifox.tvb.domain.member.domain.Profile;
import com.gravifox.tvb.domain.member.domain.SocialLogin;
import com.gravifox.tvb.domain.member.domain.user.User;
import com.gravifox.tvb.domain.member.domain.user.LoginType;
import com.gravifox.tvb.domain.member.repository.PasswordRepository;
import com.gravifox.tvb.domain.member.repository.ProfileRepository;
import com.gravifox.tvb.domain.member.repository.SocialLoginRepository;
import com.gravifox.tvb.domain.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {
    private final SocialLoginRepository socialLoginRepository;
    private final PasswordRepository passwordRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        //TODO: 개발자 구현 로직

        String providerId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        log.info("OAuth2 login requested: providerId={}, email={}", providerId, email);

        SocialLogin socialLogin = socialLoginRepository.findBySocialId(email)
                .orElseGet(() -> registerSocial(providerId, email, name));
        log.info("Registered Social Login: {}", socialLogin);
        return oAuth2User;
    }

    private SocialLogin registerSocial(String providerId, String email, String name) {
        User user = User.builder()
                .userId(email)
                .loginType(LoginType.GOOGLE)
                .build();
        userRepository.save(user);

        log.info("New user registered via social login: userId={}, email={}", user.getUserId(), email);

        Profile profile = Profile.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .nickname(name)
                .user(user)
                .build();
        profileRepository.save(profile);
        // 양방향 관계 동기화
        user.setProfile(profile);
        log.info("Profile created for social login: nickname={}", name);

        Password password = Password.builder()
                .password(java.util.UUID.randomUUID().toString())
                .updatedAt(LocalDateTime.now())
                .user(user)
                .build();
        passwordRepository.save(password);
        log.info("{}", password);

        return socialLoginRepository.save(SocialLogin.builder()
                .providerId(providerId)
                .socialId(email)
                .user(user)
                .build()
        );
    }
}
