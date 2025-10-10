package com.gravifox.interfaces.web.common;

import com.gravifox.domain.member.exception.common.ErrorCode;
import com.gravifox.domain.member.exception.common.ErrorMessageMap;
import com.gravifox.exception.GlobalException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static ResponseEntity<?> toResponse(ErrorCode code, HttpServletRequest request) {
        HttpStatus status = Optional.ofNullable(code.getHttpStatus()).orElse(HttpStatus.INTERNAL_SERVER_ERROR);

        // If the request negotiated Server-Sent Events, return SSE-friendly error text
        if (isSseRequest(request)) {
            String payload = sseErrorPayload(code.getCode(), code.getMessage());
            return ResponseEntity.status(status)
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(payload);
        }

        ErrorMessageMap body = new ErrorMessageMap(code.getCode(), code.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    private static boolean isSseRequest(HttpServletRequest request) {
        if (request == null) return false;
        String accept = Optional.ofNullable(request.getHeader(HttpHeaders.ACCEPT)).orElse("");
        String uri = Optional.ofNullable(request.getRequestURI()).orElse("");
        boolean acceptSse = accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE);
        boolean sseEndpoint = uri.startsWith("/api/analyze/") && uri.endsWith("/events");
        return acceptSse || sseEndpoint;
    }

    private static String sseErrorPayload(String code, String message) {
        String safeCode = escapeForJson(code);
        String safeMsg = escapeForJson(message);
        return "event: error\n" +
                "data: {\"code\":\"" + safeCode + "\",\"message\":\"" + safeMsg + "\"}\n\n";
    }

    private static String escapeForJson(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<?> handleGlobal(GlobalException e, HttpServletRequest request) {
        return toResponse(e.getErrorCode(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        return toResponse(ErrorCode.REQUEST_VALIDATION_ERROR, request);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<?> handleBadRequest(Exception e, HttpServletRequest request) {
        return toResponse(ErrorCode.REQUEST_VALIDATION_ERROR, request);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(org.springframework.security.access.AccessDeniedException e, HttpServletRequest request) {
        return toResponse(ErrorCode.INVALID_CREDENTIALS, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnhandled(Exception e, HttpServletRequest request) {
        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR, request);
    }
}
