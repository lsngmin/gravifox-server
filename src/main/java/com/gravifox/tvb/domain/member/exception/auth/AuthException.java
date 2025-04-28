package com.gravifox.tvb.domain.member.exception.auth;

import com.gravifox.tvb.domain.member.exception.common.ErrorCode;
import com.gravifox.tvb.exception.GlobalException;
import lombok.Getter;

@Getter
public class AuthException extends GlobalException {
    private final ErrorCode errorCode;

    public AuthException(ErrorCode e) {
        super(e);
        this.errorCode = e;
    }
    public AuthException(ErrorCode e, String o) {
        super(e, o);
        this.errorCode = e;
    }
}
