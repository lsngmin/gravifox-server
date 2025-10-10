package com.gravifox.domain.member.domain.user;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LoginTypeConvertor implements AttributeConverter<LoginType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(LoginType loginType) {
        if (loginType == null) {
            return null;
        }
        return loginType.getValue();
    }

    @Override
    public LoginType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return LoginType.fromValue(dbData);
    }
}
