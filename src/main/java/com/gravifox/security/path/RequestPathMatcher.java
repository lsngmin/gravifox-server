package com.gravifox.security.path;

import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * 중앙 집중식 퍼블릭 경로 매칭 유틸리티.
 * Ant 패턴으로 요청 경로가 공개(인증 제외) 대상인지 판단합니다.
 */
public final class RequestPathMatcher {
    private static final AntPathMatcher matcher = new AntPathMatcher();

    // 인증 없이 허용할 엔드포인트 패턴들
    public static final List<String> PUBLIC_PATTERNS = List.of(
            "/health/**",
            "/docs/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/api/upload-swagger",
            "/api/v1/register/**",
            "/api/v1/auth/**",
            "/api/v1/auth/email/**",
            "/api/analyze/**",
            "/upload",
            "/upload/**",
            "/api/v1/files/upload"
    );

    private RequestPathMatcher() {}

    public static boolean isPublicPath(String uri) {
        if (uri == null || uri.isBlank()) return false;
        for (String pattern : PUBLIC_PATTERNS) {
            if (matcher.match(pattern, uri)) return true;
        }
        return false;
    }
}

