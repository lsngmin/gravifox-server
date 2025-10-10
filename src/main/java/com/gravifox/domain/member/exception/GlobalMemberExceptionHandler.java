package com.gravifox.domain.member.exception;

import com.gravifox.domain.member.exception.auth.AuthException;
import com.gravifox.domain.member.exception.common.ErrorCode;
import com.gravifox.domain.member.exception.common.ErrorMessageMap;
import com.gravifox.domain.member.exception.register.RegisterException;
import com.gravifox.logging.util.LogUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalMemberExceptionHandler {
    private final LogUtil logUtil;
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorMessageMap> handleAuthException(AuthException e) {
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(
                new ErrorMessageMap(
                        e.getErrorCode().getCode(),
                        e.getMessage()
                )
        );
    }

    @ExceptionHandler(RegisterException.class)
    public ResponseEntity<ErrorMessageMap> handleAuthException(RegisterException e) {
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(
                new ErrorMessageMap(
                        e.getErrorCode().getCode(),
                        e.getMessage()
                )
        );
    }
    //아래부터 회원가입 예외처리 핸들러 입니다.
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorMessageMap> handleMethodArgumentNotValidException(Exception ex) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        log.warn(logUtil.formatMessage(
                "UserRegistration",
                ex.getClass().getSimpleName(),
                ErrorCode.REQUEST_VALIDATION_ERROR.getMessage(),
                request.getMethod(),
                request.getRequestURI(),
                ErrorCode.REQUEST_VALIDATION_ERROR.getHttpStatus().value(),
               ""
        ));
        return ResponseEntity.status(ErrorCode.REQUEST_VALIDATION_ERROR.getHttpStatus()).body(
                new ErrorMessageMap(
                        ErrorCode.REQUEST_VALIDATION_ERROR.getCode(),
                        ErrorCode.REQUEST_VALIDATION_ERROR.getMessage()
                )
        );
    }
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String, String>> handleException(Exception e) {
//        Map<String, String> error = new HashMap<>();
//        error.put("error", "서버 오류가 발생했습니다.");
//        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
}
