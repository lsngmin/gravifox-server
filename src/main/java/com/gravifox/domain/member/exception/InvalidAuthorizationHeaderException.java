package com.gravifox.domain.member.exception;

import com.gravifox.domain.member.exception.auth.AuthException;
import com.gravifox.domain.member.exception.common.ErrorCode;

public class InvalidAuthorizationHeaderException extends AuthException {
    public InvalidAuthorizationHeaderException() {

        super(ErrorCode.INVALID_AUTHORIZATION_HEADER);
    }
}
