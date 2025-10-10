package com.gravifox.domain.member.exception;

import com.gravifox.domain.member.exception.auth.AuthException;
import com.gravifox.domain.member.exception.common.ErrorCode;

public class TokenNotFoundException extends AuthException {
    public TokenNotFoundException() {
        super(ErrorCode.TOKEN_NOT_FOUND);
    }
}
