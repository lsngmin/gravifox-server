package com.tvb.global.security.oauth2;

import com.tvb.domain.member.dto.register.AuthRequest;
import com.tvb.domain.member.domain.User;
import com.tvb.domain.member.repository.SocialLoginRepository;
import com.tvb.global.security.jwt.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2UserSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;
    private final SocialLoginRepository socialLoginRepository;

    @Value("${front.redirect.url}") private String url;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String socialId = oAuth2User.getAttribute("email");
        Optional<User> user = socialLoginRepository.findUserBySocialId(socialId);
        if (user.isPresent()) {
            String refreshToken = jwtUtil.createToken(
                    AuthRequest.builder()
                            .user(user.get())
                            .build().getDataMap(),
                    600);
            Cookie cookie = new Cookie("refreshToken", refreshToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);//TODO: 현재는 https 통신을 지원하지 않아 비활성화 했지만 추후 https 통신 연결시 true 바꾸어야 한다.
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            cookie.setAttribute("SameSite", "None");
            response.addCookie(cookie);
        } else {
            System.out.println("NOOOOOOO");
        }
        response.sendRedirect(url);
    }
}
