package com.gravifox.domain.member.exception.auth;

import com.gravifox.domain.member.exception.common.ErrorCode;

public class ExpiredVerificationTokenException extends AuthException {
    public ExpiredVerificationTokenException() { super(ErrorCode.TOKEN_EXPIRED); }
}

