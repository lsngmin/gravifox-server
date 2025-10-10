package com.gravifox.tvb.domain.member.controller;

import com.gravifox.tvb.annotation.LogContext;
import com.gravifox.tvb.domain.member.dto.login.LoginRequest;
import com.gravifox.tvb.domain.member.dto.login.LoginResponse;
import com.gravifox.tvb.domain.member.exception.common.ErrorMessageMap;
import com.gravifox.tvb.domain.member.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

//
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
        name = "인증",
        description = """
            인증과 관련된 API입니다.
            사용자가 로그인하면 Access Token과 Refresh Token을 발급받고,
            이를 통해 사용자 인증 및 인가 처리를 수행합니다.
            - 로그인: 사용자 정보를 기반으로 토큰을 발급받습니다.
            - 토큰 갱신: Access Token 만료 시 Refresh Token으로 재발급합니다.
            - 사용자 조회: 발급된 Access Token으로 사용자 정보를 확인합니다.
            - 로그아웃: Refresh Token 쿠키를 제거하여 세션을 종료합니다.
    """
)
public class AuthController {
    private final AuthService authService;
    @org.springframework.beans.factory.annotation.Value("${app.cookie.secure:true}")
    private boolean cookieSecure;
    @org.springframework.beans.factory.annotation.Value("${app.cookie.same-site:None}")
    private String cookieSameSite;
    @org.springframework.beans.factory.annotation.Value("${app.cookie.max-age-days:7}")
    private int cookieMaxAgeDays;
    @org.springframework.beans.factory.annotation.Value("${app.cookie.domain:}")
    private String cookieDomain;

    @Operation(
            summary = "사용자 로그인",
            description = "이메일(ID)과 비밀번호를 입력하여 로그인을 시도합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "로그인 요청 예시",
                                    summary = "정상 로그인 요청",
                                    value = """
                    {
                        "user": {
                            "userId": "user@example.com",
                        },
                        "password": {
                            "password": "my_password_123!"
                        }
                    }
                """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "422", description = "유효하지 않은 사용자 ID 또는 비밀번호", content = @Content(schema = @Schema(implementation = ErrorMessageMap.class))
                    )
            }
    )

    @PostMapping("/login")
    @LogContext(action = "UserAuthentication", detail="UserId")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        Map<String, String> token = authService.makeTokenAndLogin(loginRequest);
        response.addCookie(authService.storeRefreshTokenInCookie(token.get("refreshToken")));
        // 로그인 성공 시 게스트 쿠키 제거
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
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(token.get("accessToken"));
        loginResponse.setUserId(token.get("userId"));
        return ResponseEntity.ok(loginResponse);

//        return Optional.ofNullable(authService.makeTokenAndLogin(loginRequest))
//                .map(tokenResponse -> Pair.of(
//                        authService.storeRefreshTokenInCookie(tokenResponse.get("refreshToken")),
//                        tokenResponse.get("accessToken")
//                ))
//                .map(pair -> {
//                    response.addCookie(pair.getFirst());
//                    return ResponseEntity.ok(Map.of("accessToken", pair.getSecond()));
//                })
//                .orElse(new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    /**
     *
     * 왜 토큰 파라미터의 required 가 false인가요 ? - true로 설정할 경우 토큰이 없으면 400 Bad Request를 반환하게 됩니다.
     *  하지만 이 경우에도 요청을 수용하여 200 OK를 반환함으로 UX를 저해하지 않고 부드럽게 처리할 수 있습니다.
     *
     * @param accessToken
     * @param refreshToken
     * @param response
     * @return
     */

    @Operation(
            summary = "JWT 토큰 갱신",
            description = """
            클라이언트가 보유한 Refresh Token을 통해 새로운 Access Token을 발급받습니다.
        
            일반적으로 Access Token이 만료된 상태에서 호출되며, 서버는:
            - Authorization 헤더에 담긴 Access Token (옵션)
            - 쿠키에 담긴 Refresh Token (필수)
            을 검증하여 새로운 JWT를 발급합니다.
        
            새롭게 발급된 Refresh Token은 쿠키에 다시 저장됩니다.
    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 토큰", content = @Content(mediaType = "application/json"))            }
    )

    @PostMapping("/refresh")
    @LogContext(action = "UserTokenRefresh", detail="ONLY REFRESH NOT AUTHENTICATION!")
    public ResponseEntity<?> refresh(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                     @CookieValue(name = "refreshToken", required = false) String refreshToken,
                                     HttpServletResponse response) {
        if(refreshToken == null) {
            // 리프레시 토큰이 없으면 게스트 상태로 간주 -> 게스트 쿠키 1
            Cookie guestCookie = new Cookie("guest", "1");
            guestCookie.setHttpOnly(false);
            guestCookie.setSecure(cookieSecure);
            guestCookie.setPath("/");
            guestCookie.setMaxAge(cookieMaxAgeDays * 24 * 60 * 60);
            guestCookie.setAttribute("SameSite", cookieSameSite);
            if (cookieDomain != null && !cookieDomain.isBlank()) {
                guestCookie.setDomain(cookieDomain);
            }
            response.addCookie(guestCookie);
            return ResponseEntity.noContent().build();
        }
        Map<String, String> map = authService.RefreshToken(accessToken, refreshToken);
        response.addCookie(authService.storeRefreshTokenInCookie(map.get("refreshToken")));
        // 리프레시 성공 시 게스트 쿠키 제거
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
        return ResponseEntity.ok(Map.of("accessToken", map.get("accessToken")));
    }

    @Operation(
            summary = "현재 사용자 정보 확인",
            description = "Access Token을 통해 로그인된 사용자의 정보를 확인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "401", description = "인증 실패 - 토큰 없음 / 유효하지 않음", content = @Content(mediaType = "application/json"))
            }
    )
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                @CookieValue(name = "refreshToken", required = false) String refreshToken) {
//        log.info("Access token: {}", accessToken);
//        log.info("Refresh token: {}", refreshToken);


        if (accessToken == null || accessToken.isBlank()) {
            //** TODO: 액세스 토큰이 없으면 리프레시토큰을 검사하고 있으면 액세스 토큰을 발급해서 사용자 정보를 가져오고
            //** TODO: 리프레시 토큰이 없으면 사용자가 인증되지 않았으므로 사용자가 존재하지 않는다고 판단함
            return ResponseEntity.ok().build();
        }
        //TODO: 액세스 토큰이 있으면 토큰의 유효성을 검사함
        return ResponseEntity.ok(authService.validateUserToken(accessToken.substring(7)));
    }


        @Operation(
                summary = "사용자 로그아웃",
                description = "Refresh Token 쿠키를 만료시켜 로그아웃 처리를 합니다.",
                responses = {
                        @ApiResponse(responseCode = "200",description = "로그아웃 성공", content = @Content(mediaType = "application/json"))
                }
        )
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String accessToken,

                                    HttpServletResponse response) {
        log.info("Logout Request: {}", response.getStatus());
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", cookieSameSite);
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            cookie.setDomain(cookieDomain);
        }
        response.addCookie(cookie);
        // 로그아웃 시 게스트 쿠키 1로 설정
        Cookie guestCookie = new Cookie("guest", "1");
        guestCookie.setHttpOnly(false);
        guestCookie.setSecure(cookieSecure);
        guestCookie.setPath("/");
        guestCookie.setMaxAge(cookieMaxAgeDays * 24 * 60 * 60);
        guestCookie.setAttribute("SameSite", cookieSameSite);
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            guestCookie.setDomain(cookieDomain);
        }
        response.addCookie(guestCookie);
        return ResponseEntity.ok().build();
    }
}
