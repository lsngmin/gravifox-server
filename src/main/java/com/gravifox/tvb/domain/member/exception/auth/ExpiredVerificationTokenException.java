package com.gravifox.tvb.domain.member.exception.auth;

import com.gravifox.tvb.domain.member.exception.common.ErrorCode;

public class ExpiredVerificationTokenException extends AuthException {
    public ExpiredVerificationTokenException() { super(ErrorCode.TOKEN_EXPIRED); }
}

