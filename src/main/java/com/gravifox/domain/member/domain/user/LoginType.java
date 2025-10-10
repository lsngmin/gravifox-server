package com.gravifox.domain.member.domain.user;

import com.gravifox.domain.member.exception.IllegalLoginTypeArgumentException;
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
    public static LoginType fromString(String value) {
        for(LoginType type : LoginType.values()) {
            if(type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalLoginTypeArgumentException();
    }
}
