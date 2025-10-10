package com.gravifox.domain.member.exception.register;

import com.gravifox.domain.member.exception.common.ErrorCode;

import static com.gravifox.domain.member.exception.common.ErrorCode.*;

public class InvalidFormatException extends RegisterException {
    public static InvalidFormatException forInvalidNickName(String n) {
        return new InvalidFormatException(INVALID_NICKNAME_ERROR, n);
    }
    public static InvalidFormatException forInvalidUserId(String n) {
        return new InvalidFormatException(INVALID_USERID_ERROR, n);
    }
    public static InvalidFormatException forInvalidPassword(String n) {
        return new InvalidFormatException(INVALID_PASSWORD_ERROR, n);
    }
    public static InvalidFormatException forInvalidLoginType(String n) {
        return new InvalidFormatException(INVALID_LOGINTYPE_ERROR, n);
    }

    public InvalidFormatException(ErrorCode e, String o) {
        super(e, o);
    }
    public InvalidFormatException() {
        super(ErrorCode.REQUEST_VALIDATION_ERROR, "");
    }
}
