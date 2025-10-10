package com.gravifox.security.jwt.filter;

import com.gravifox.security.jwt.principal.UserPrincipal;
import com.gravifox.domain.member.exception.common.ErrorCode;
import com.gravifox.security.jwt.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth") || path.startsWith("/api/v1/register") ||
                path.startsWith("/api/v1/auth/refresh") || path.startsWith("/health") ||
                path.startsWith("/api/v1/auth/**") || path.startsWith("/api/v1/images/send_data") ||
                path.startsWith("/api/v1/issue/") || path.startsWith("/docs") || path.startsWith("/v3/api-docs") ||
                path.startsWith("/docs/") || path.startsWith("/swagger-resources/**") || path.startsWith("/api/upload-swagger") ||
                path.startsWith("/api/v1/auth/email/**") || path.startsWith("/api/analyze") || path.startsWith("/api/analyze/**") ||
                path.equals("/upload") || path.startsWith("/upload/") || path.startsWith("/api/v1/files/upload");

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String headerStr = request.getHeader("Authorization");
        if(headerStr == null || !headerStr.startsWith("Bearer ")) {
            handleException(response, new Exception("Access Token Not Found."));
            return;
        }
        String accessToken = headerStr.substring(7);
        try {
            java.util.Map<String, Object> tokenMap = jwtUtil.validateToken(accessToken);
            String userNo = tokenMap.get("userNo").toString();

            //TODO : roles라는 역할이 추가될 때 마찬가지로 roles를 받아오는 코드 추가 필요
            //TODO : 지금의 역할은 User 외 존재 X. 따라서 User로 역할을 고정한다.
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
            handleException(response, e);
        }

    }
    private void handleException(HttpServletResponse response, Exception e) throws IOException {
        ErrorCode mapped = (e != null && e.getMessage() != null && e.getMessage().toLowerCase().contains("not found"))
                ? ErrorCode.TOKEN_NOT_FOUND
                : ErrorCode.TOKEN_INVALID;
        response.setStatus(mapped.getHttpStatus().value());
        response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
        response.setContentType("application/json");
        String json = String.format("{\"code\":\"%s\",\"message\":\"%s\"}", mapped.getCode(), mapped.getMessage());
        response.getWriter().println(json);
    }
}
