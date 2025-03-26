package com.tvb.api.domain.member.service.impl;

import com.tvb.api.domain.member.dto.AuthRequest;
import com.tvb.api.domain.member.entity.User;
import com.tvb.api.domain.member.exception.IllegalLoginTypeArgumentException;
import com.tvb.api.domain.member.exception.InvalidAuthorizationHeaderException;
import com.tvb.api.domain.member.exception.InvalidCredentialsException;
import com.tvb.api.domain.member.repository.UserRepository;
import com.tvb.api.domain.member.repository.PasswordRepository;
import com.tvb.api.domain.member.service.AuthService;
import com.tvb.api.jwt.security.util.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PasswordRepository passwordRepository;

    @Override
    public Map<String, String> makeTokenAndLogin(AuthRequest authRequest) {
        Optional<User> user = userRepository.findByUserId(authRequest.getUser().getUserId());

        Optional<String> password = passwordRepository.findPasswordByUser(
                user.orElseThrow(IllegalLoginTypeArgumentException::new
        ));

        if (password.isPresent() &&
                passwordEncoder.matches(
                        authRequest.getPassword().getPassword(), password.get())) {
            authRequest.changeUser(user.get());
            Map<String, String> dataMap = authRequest.getDataMap();
            String accessToken = jwtUtil.createToken(dataMap, 1);
            String refreshToken = jwtUtil.createToken(dataMap, 9999999);
            return Map.of("accessToken", accessToken, "refreshToken",refreshToken);
        }
        throw new InvalidCredentialsException();
    }

    @Override
    public Map<String, String> RefreshToken(String accessToken, String refreshToken) {
        if(accessToken != null && !accessToken.isBlank()) {
            accessToken = Optional.of(accessToken)
                    .filter(token -> token.startsWith("Bearer "))
                    .map(token -> token.substring(7))
                    .orElseThrow(InvalidAuthorizationHeaderException::new);
            try {
                jwtUtil.validateToken(accessToken);
                log.info("Token validation successful.");
                return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
            } catch (io.jsonwebtoken.ExpiredJwtException expiredJwtException) {
                log.info("Access token expired.");
                return makeNewToken(refreshToken);
            } catch (Exception e) {
                //TODO: throw error 변경 필요
                throw new RuntimeException(e.getMessage());
            }
        } else {
            return makeNewToken(refreshToken);
        }
    }

    @Override
    public Cookie storeRefreshTokenInCookie(String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);//TODO: 현재는 https 통신을 지원하지 않아 비활성화 했지만 추후 https 통신 연결시 true 바꾸어야 한다.
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        return cookie;
    }

    public Map<String, Object> validateUserToken(String accessToken) {
        return jwtUtil.validateToken(accessToken);
    }

    private  Map<String, String> makeNewToken(String refreshToken) {
            Map<String, Object> claims = jwtUtil.validateToken(refreshToken);
            Map<String, String> dataMap = Map.of("userId", String.valueOf(claims.get("userId")), "userNo", String.valueOf(claims.get("userNo")));

            log.info("claims: {}", claims);

            String newAccessToken = jwtUtil.createToken(dataMap, 999);
            String newRefreshToken = jwtUtil.createToken(dataMap, 999);

            return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);
    }


}