package com.gravifox.security.jwt.filter;

import com.gravifox.security.jwt.principal.UserPrincipal;
import com.gravifox.domain.member.exception.common.ErrorCode;
import com.gravifox.security.jwt.util.JWTUtil;
import com.gravifox.security.path.RequestPathMatcher;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
@Slf4j
@Component
@RequiredArgsConstructor
public class JWTCheckFilter extends OncePerRequestFilter {
    @Autowired
    private JWTUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String requestUri = request.getRequestURI();
        return RequestPathMatcher.isPublicPath(requestUri);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 테스트/내부 인증 주입(@WithMockUser 등) 시 이미 인증이 존재하면 토큰 검사 생략
        var existingAuth = SecurityContextHolder.getContext().getAuthentication();
        if (existingAuth != null && existingAuth.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }
        String headerStr = request.getHeader("Authorization");
        if (!StringUtils.hasText(headerStr)) {
            if (log.isWarnEnabled()) {
                log.warn("Authorization header missing for uri={}, method={}, origin={}",
                        request.getRequestURI(), request.getMethod(), request.getHeader("Origin"));
            }
            throw new BadCredentialsException(ErrorCode.TOKEN_NOT_FOUND.getMessage());
        }
        String trimmed = headerStr.trim();
        if (!trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            if (log.isWarnEnabled()) {
                log.warn("Authorization header malformed for uri={}, value={}", request.getRequestURI(), headerStr);
            }
            throw new BadCredentialsException(ErrorCode.TOKEN_NOT_FOUND.getMessage());
        }
        String accessToken = trimmed.substring(7).trim();
        if (!StringUtils.hasText(accessToken)) {
            if (log.isWarnEnabled()) {
                log.warn("Authorization header without token for uri={}, rawHeader={}", request.getRequestURI(), headerStr);
            }
            throw new BadCredentialsException(ErrorCode.TOKEN_NOT_FOUND.getMessage());
        }
        try {
            java.util.Map<String, Object> tokenMap = jwtUtil.validateToken(accessToken);
            String userNo = tokenMap.get("userNo").toString();

            String[] roles = {"User"};

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    new UserPrincipal(userNo), null, Arrays.stream(roles)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList())
            );

            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            throw new BadCredentialsException(ErrorCode.TOKEN_INVALID.getMessage(), e);
        }
    }
}
