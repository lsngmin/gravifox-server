package com.gravifox.domain.file.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UploadTokenUnauthorizedException extends RuntimeException {
    public UploadTokenUnauthorizedException(String message) {
        super(message);
    }
}
