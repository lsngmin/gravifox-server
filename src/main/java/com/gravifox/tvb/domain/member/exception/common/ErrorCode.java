package com.gravifox.tvb.domain.member.exception.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Oops! Something doesn’t match."),
    EMAIL_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, "EMAIL_NOT_VERIFIED", "Oops! Something doesn’t match."),
    TOKEN_INVALID(HttpStatus.BAD_REQUEST, "TOKEN_INVALID", "Invalid or expired link."),
    TOKEN_EXPIRED(HttpStatus.GONE, "TOKEN_EXPIRED", "Verification link has expired."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."),

    INVALID_AUTHORIZATION_HEADER(HttpStatus.UNAUTHORIZED, "INVALID_AUTHORIZATION_HEADER", "Authorization header must start with 'Bearer'."),

    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "TOKEN_NOT_FOUND", "Token Not Found."),


    //회원가입에서 발생하는 에러 코드입니다.
    REGISTRATION_FAILURE(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_USER_REQUEST", "Registration failed."),
    DUPLICATE_USER_ID(HttpStatus.UNPROCESSABLE_ENTITY, "DUPLICATE_USER_ID", "UserId already in use"),
    REQUEST_VALIDATION_ERROR(HttpStatus.UNPROCESSABLE_ENTITY, "REQUEST_VALIDATION_ERROR", "The request data contains invalid fields"),
    INVALID_PASSWORD_ERROR(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_PASSWORD_ERROR", "Password must be 8–20 characters long and include letters, numbers, and special characters"),
    INVALID_USERID_ERROR(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_USERID_ERROR", "Please enter a valid email address"),
    INVALID_NICKNAME_ERROR(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_NICKNAME_ERROR", "NickName cannot be blank"),
    INVALID_LOGINTYPE_ERROR(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_LOGINTYPE_ERROR", "The request data contains invalid fields"),

    USER_NOT_FOUND(HttpStatus.UNPROCESSABLE_ENTITY, "USER_NOT_FOUND", "User information could not be found.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }


}
