package com.gravifox.tvb.domain.member.service.impl;

import com.gravifox.tvb.annotation.LogContext;
import com.gravifox.tvb.domain.member.dto.login.LoginRequest;
import com.gravifox.tvb.domain.member.domain.user.User;
import com.gravifox.tvb.domain.member.exception.InvalidAuthorizationHeaderException;
import com.gravifox.tvb.domain.member.exception.InvalidCredentialsException;
import com.gravifox.tvb.domain.member.repository.UserRepository;
import com.gravifox.tvb.domain.member.repository.PasswordRepository;
import com.gravifox.tvb.domain.member.service.AuthService;
import com.gravifox.tvb.security.jwt.util.JWTUtil;
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

    @Override @LogContext(action = "UserAuthentication", detail = "UserId")
    public Map<String, String> makeTokenAndLogin(LoginRequest loginRequest) {
        String userId = loginRequest.getUser().getUserId();
        Optional<User> user = userRepository.findByUserId(userId);

        Optional<String> password = passwordRepository.findPasswordByUser(
                user.orElseThrow(InvalidCredentialsException::new
        ));

        if (password.isPresent() &&
                passwordEncoder.matches(
                        loginRequest.getPassword().getPassword(), password.get())) {
            loginRequest.changeUser(user.get());
            Map<String, String> dataMap = loginRequest.getDataMap();
            String accessToken = jwtUtil.createToken(dataMap, 1);
            String refreshToken = jwtUtil.createToken(dataMap, 9999999);
            return Map.of("accessToken", accessToken, "refreshToken",refreshToken, "userId", userId);
        }
        throw new InvalidCredentialsException(userId);
    }

    /**
     * AccessToken이 만료되었거나 새로고침으로 없어졌을 때 재발급을 위한 엔드포인트입니다.
     *
     * @param accessToken - 없어도 됩니다. 만약 없다면 리프레시 토큰으로 makeNewToken()을 호출합니다.
     *                    있다면 해당 토큰을 검증합니다. 성공하면 가지고 있는 토큰을 반환합니다. 실패하면 리프레시 토큰으로 AccessToken을 재발급합니다.
     *                    그 외에 에러는 런타임 에러를 던집니다.
     * @param refreshToken - 필수적으로 있어야 합니다. 없다면 에러를 반환합니다.
     * @return - Map 형태로 AccessToken , RefreshToken을 가지고 있습니다.
     */
    @Override @LogContext(action = "TokenRefresh", detail = "UserId")
    public Map<String, String> RefreshToken(String accessToken, String refreshToken) {
        if(accessToken != null && !accessToken.isBlank()) {
            accessToken = Optional.of(accessToken)
                    .filter(token -> token.startsWith("Bearer "))
                    .map(token -> token.substring(7))
                    .orElseThrow(InvalidAuthorizationHeaderException::new);
            try {
                jwtUtil.validateToken(accessToken);
                return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
            } catch (io.jsonwebtoken.ExpiredJwtException expiredJwtException) {
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
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        cookie.setAttribute("SameSite", "None");
        return cookie;
    }

    public Map<String, Object> validateUserToken(String accessToken) {
        return jwtUtil.validateToken(accessToken);
    }

    private  Map<String, String> makeNewToken(String refreshToken) {
            Map<String, Object> claims = jwtUtil.validateToken(refreshToken);
            Map<String, String> dataMap = Map.of("userId", String.valueOf(claims.get("userId")), "userNo", String.valueOf(claims.get("userNo")));

            String newAccessToken = jwtUtil.createToken(dataMap, 999);
            String newRefreshToken = jwtUtil.createToken(dataMap, 999);

            return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);
    }
}
