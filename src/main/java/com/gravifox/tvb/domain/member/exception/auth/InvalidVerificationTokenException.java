package com.gravifox.tvb.domain.member.exception.auth;

import com.gravifox.tvb.domain.member.exception.common.ErrorCode;

public class InvalidVerificationTokenException extends AuthException {
    public InvalidVerificationTokenException() { super(ErrorCode.TOKEN_INVALID); }
    public InvalidVerificationTokenException(String token) { super(ErrorCode.TOKEN_INVALID, token); }
}

