package com.gravifox.domain.file.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UploadTokenConflictException extends RuntimeException {
    public UploadTokenConflictException(String message) {
        super(message);
    }
}
