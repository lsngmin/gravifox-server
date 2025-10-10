package com.gravifox.interfaces.web.common;

import com.gravifox.domain.member.exception.common.ErrorCode;
import com.gravifox.domain.member.exception.common.ErrorMessageMap;
import com.gravifox.exception.GlobalException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static ResponseEntity<ErrorMessageMap> toResponse(ErrorCode code) {
        HttpStatus status = Optional.ofNullable(code.getHttpStatus()).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorMessageMap body = new ErrorMessageMap(code.getCode(), code.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ErrorMessageMap> handleGlobal(GlobalException e) {
        return toResponse(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessageMap> handleValidation(MethodArgumentNotValidException e) {
        return toResponse(ErrorCode.REQUEST_VALIDATION_ERROR);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorMessageMap> handleBadRequest(Exception e) {
        return toResponse(ErrorCode.REQUEST_VALIDATION_ERROR);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorMessageMap> handleAccessDenied(org.springframework.security.access.AccessDeniedException e) {
        return toResponse(ErrorCode.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessageMap> handleUnhandled(Exception e) {
        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}

