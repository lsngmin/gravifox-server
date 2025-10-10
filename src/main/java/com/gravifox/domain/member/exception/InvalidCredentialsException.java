package com.gravifox.domain.member.exception;

import com.gravifox.domain.member.exception.auth.AuthException;
import com.gravifox.domain.member.exception.common.ErrorCode;

public class InvalidCredentialsException extends AuthException {
    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }
    public InvalidCredentialsException(String o) {
        super(ErrorCode.INVALID_CREDENTIALS,o);
    }

}
