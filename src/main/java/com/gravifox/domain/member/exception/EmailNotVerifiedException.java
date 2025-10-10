package com.gravifox.domain.member.exception;

import com.gravifox.domain.member.exception.auth.AuthException;
import com.gravifox.domain.member.exception.common.ErrorCode;

public class EmailNotVerifiedException extends AuthException {
    public EmailNotVerifiedException() { super(ErrorCode.EMAIL_NOT_VERIFIED); }
    public EmailNotVerifiedException(String userId) { super(ErrorCode.EMAIL_NOT_VERIFIED, userId); }
}

