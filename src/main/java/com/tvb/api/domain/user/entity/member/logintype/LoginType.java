package com.tvb.api.domain.user.entity.member.logintype;

import com.tvb.api.domain.user.exception.IllegalLoginTypeArgumentException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoginType {
    EMAIL(181),
    GOOGLE(191);

    private final int value;

    public static LoginType fromValue(int value) {
        for(LoginType type : LoginType.values()) {
            if(type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalLoginTypeArgumentException();
    }
}
