package com.gravifox.tvb.domain.member.service.oauth2;

import com.gravifox.tvb.domain.member.dto.login.LoginRequest;
import com.gravifox.tvb.domain.member.domain.user.User;
import com.gravifox.tvb.domain.member.repository.SocialLoginRepository;
import com.gravifox.tvb.domain.member.repository.UserRepository;
import com.gravifox.tvb.security.jwt.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2UserSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;
    private final SocialLoginRepository socialLoginRepository;
    private final UserRepository userRepository;

    @Value("${front.redirect.url}") private String url;
    @Value("${app.cookie.secure:true}") private boolean cookieSecure;
    @Value("${app.cookie.same-site:None}") private String cookieSameSite;
    @Value("${app.cookie.max-age-days:7}") private int cookieMaxAgeDays;
    @Value("${app.cookie.domain:}") private String cookieDomain;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("onAuthenticationSuccessayuthen: {}", authentication);
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        log.info("onAuthenticationSuccessoau2: {}", oAuth2User);
        String socialId = oAuth2User.getAttribute("email");
        log.info("OAuth2 authentication success. Social email={}", socialId);

        Optional<User> user = socialLoginRepository.findUserBySocialId(socialId);
        log.info("onAuthenticationSuccessuss: {}", user);
        if (user.isPresent()) {
            log.info("User found. userId={}, socialEmail={}", user.get().getUserId(), socialId);

            // JWT 클레임의 userId는 소셜 이메일로 저장하여 프론트에 이메일이 노출되도록 함
            java.util.Map<String, String> claims = java.util.Map.of(
                    "userId", socialId,
                    "userNo", String.valueOf(user.get().getUserNo())
            );
            String refreshToken = jwtUtil.createToken(claims, 7 * 24 * 60);
            Cookie cookie = new Cookie("refreshToken", refreshToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(cookieSecure);// 환경에 따라 보안 플래그 분기
            cookie.setPath("/");
            cookie.setMaxAge(cookieMaxAgeDays * 24 * 60 * 60);
            cookie.setAttribute("SameSite", cookieSameSite);
            if (cookieDomain != null && !cookieDomain.isBlank()) {
                cookie.setDomain(cookieDomain);
            }
            response.addCookie(cookie);
            log.info("Refresh token set in cookie for userId={}", user.get().getUserId());

            // 로그인 성공(소셜) 시 게스트 쿠키 제거
            Cookie guestCookie = new Cookie("guest", null);
            guestCookie.setHttpOnly(false);
            guestCookie.setSecure(cookieSecure);
            guestCookie.setPath("/");
            guestCookie.setMaxAge(0);
            guestCookie.setAttribute("SameSite", cookieSameSite);
            if (cookieDomain != null && !cookieDomain.isBlank()) {
                guestCookie.setDomain(cookieDomain);
            }
            response.addCookie(guestCookie);

        } else {
            log.warn("OAuth2 login succeeded but user not found for email={}", socialId);
        }
        log.info("url received: {}", url);
        response.sendRedirect(url);
    }
}
