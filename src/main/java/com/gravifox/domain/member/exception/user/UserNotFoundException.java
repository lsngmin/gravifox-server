package com.gravifox.domain.member.exception.user;

import com.gravifox.domain.member.exception.common.ErrorCode;

public class UserNotFoundException extends UserException {

    public UserNotFoundException(Long userNo) {
        super(ErrorCode.USER_NOT_FOUND, String.valueOf(userNo));
    }
}
