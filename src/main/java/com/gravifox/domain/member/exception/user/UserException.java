package com.gravifox.domain.member.exception.user;

import com.gravifox.domain.member.exception.common.ErrorCode;
import com.gravifox.exception.GlobalException;
import lombok.Getter;

@Getter
public class UserException extends GlobalException {

    private final ErrorCode errorCode;

    public UserException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public UserException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
        this.errorCode = errorCode;
    }
}
