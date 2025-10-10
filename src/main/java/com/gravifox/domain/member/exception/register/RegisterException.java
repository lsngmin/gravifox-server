package com.gravifox.domain.member.exception.register;

import com.gravifox.domain.member.exception.common.ErrorCode;
import com.gravifox.exception.GlobalException;
import lombok.Getter;

@Getter
public class RegisterException extends GlobalException {
  private final ErrorCode errorCode;

  public RegisterException(ErrorCode e) {
    super(e);
    this.errorCode = e;
  }
  public RegisterException(ErrorCode e, String o) {
    super(e, o);
    this.errorCode = e;
  }
}
