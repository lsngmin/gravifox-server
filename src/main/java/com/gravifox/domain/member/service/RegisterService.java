package com.gravifox.domain.member.service;

import com.gravifox.domain.member.dto.register.RegisterRequest;
import com.gravifox.domain.member.dto.register.RegisterResponse;

public interface RegisterService {
    RegisterResponse toRegisterUser(RegisterRequest registerRequestData);
}
