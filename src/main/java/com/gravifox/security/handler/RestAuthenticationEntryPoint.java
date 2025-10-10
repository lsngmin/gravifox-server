package com.gravifox.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gravifox.domain.member.exception.common.ErrorCode;
import com.gravifox.domain.member.exception.common.ErrorMessageMap;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ErrorMessageMap body = new ErrorMessageMap(ErrorCode.TOKEN_NOT_FOUND.getCode(), ErrorCode.TOKEN_NOT_FOUND.getMessage());
        response.setStatus(ErrorCode.TOKEN_NOT_FOUND.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), body);
    }
}

