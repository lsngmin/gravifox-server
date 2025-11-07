package com.gravifox.domain.member.service.oauth2;

import com.gravifox.domain.member.domain.Password;
import com.gravifox.domain.member.domain.Profile;
import com.gravifox.domain.member.domain.SocialLogin;
import com.gravifox.domain.member.domain.user.User;
import com.gravifox.domain.member.domain.user.LoginType;
import com.gravifox.domain.member.repository.PasswordRepository;
import com.gravifox.domain.member.repository.ProfileRepository;
import com.gravifox.domain.member.repository.SocialLoginRepository;
import com.gravifox.domain.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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
        User existingUser = userRepository.findByUserId(email).orElse(null);
        User persistedUser;
        if (existingUser == null) {
            User newUser = User.builder()
                    .userId(email)
                    .loginType(LoginType.GOOGLE)
                    .emailVerified(false)
                    .build();
            persistedUser = userRepository.save(newUser);
        } else {
            persistedUser = existingUser;
        }

        if (!Boolean.TRUE.equals(persistedUser.getEmailVerified())) {
            persistedUser.verifyEmail();
            userRepository.save(persistedUser);
        }

        final User user = persistedUser;

        profileRepository.findByUser(user).orElseGet(() -> {
            String baseNickname = (name != null && !name.isBlank()) ? name : email;
            String nickname = generateUniqueNickname(baseNickname);
            Profile profile = Profile.builder()
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .nickname(nickname)
                    .user(user)
                    .build();
            Profile saved = profileRepository.save(profile);
            user.setProfile(saved);
            return saved;
        });

        passwordRepository.findByUser(user).orElseGet(() -> passwordRepository.save(
                Password.builder()
                        .password(java.util.UUID.randomUUID().toString())
                        .updatedAt(LocalDateTime.now())
                        .user(user)
                        .build()
        ));

        Optional<SocialLogin> existingSocial = socialLoginRepository.findByUser(user);
        if (existingSocial.isPresent()) {
            return existingSocial.get();
        }

        return socialLoginRepository.save(SocialLogin.builder()
                .providerId(providerId)
                .socialId(email)
                .user(user)
                .build()
        );
    }

    private String generateUniqueNickname(String base) {
        String sanitized = base == null ? "user" : base.trim();
        if (sanitized.isEmpty()) {
            sanitized = "user";
        }
        String candidate = sanitized;
        int suffix = 1;
        while (profileRepository.findByNickname(candidate).isPresent()) {
            candidate = sanitized + suffix;
            suffix++;
            if (suffix > 1000) {
                candidate = sanitized + java.util.UUID.randomUUID().toString().substring(0, 8);
                break;
            }
        }
        return candidate;
    }
}
