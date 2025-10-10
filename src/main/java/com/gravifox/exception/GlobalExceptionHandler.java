package com.gravifox.exception;

import com.gravifox.domain.member.exception.common.ErrorCode;
import com.gravifox.domain.member.exception.common.ErrorMessageMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static ResponseEntity<ErrorMessageMap> build(ErrorCode code) {
        HttpStatus status = Optional.ofNullable(code.getHttpStatus()).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorMessageMap body = new ErrorMessageMap(code.getCode(), code.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ErrorMessageMap> handleGlobal(GlobalException e) {
        return build(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessageMap> handleValidation(MethodArgumentNotValidException e) {
        return build(ErrorCode.REQUEST_VALIDATION_ERROR);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorMessageMap> handleBadRequest(Exception e) {
        return build(ErrorCode.REQUEST_VALIDATION_ERROR);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorMessageMap> handleDenied(org.springframework.security.access.AccessDeniedException e) {
        // 재사용 가능한 코드로 매핑 (별도 코드가 없으므로 인증 오류 코드로 응답)
        return build(ErrorCode.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessageMap> handleOthers(Exception e) {
        return build(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}

