package com.gravifox.domain.file.service.impl;

import com.gravifox.domain.file.service.FileService;
import com.gravifox.security.jwt.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {
    private final JWTUtil jwtUtil;

    @Override
    public boolean validateUserToken(String accessToken) {
        log.info("Validating user token {}", jwtUtil.isValidToken.test(accessToken));
        return jwtUtil.isValidToken.test(accessToken);
    }
}
