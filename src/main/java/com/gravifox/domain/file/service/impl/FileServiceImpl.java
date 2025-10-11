package com.gravifox.domain.file.service.impl;

import com.gravifox.domain.file.service.FileService;
import com.gravifox.domain.file.token.UploadAuthorizationContext;
import com.gravifox.domain.file.token.UploadTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {
    private final UploadTokenService uploadTokenService;

    @Override
    public UploadAuthorizationContext authorizeUpload(String token) {
        UploadAuthorizationContext context = uploadTokenService.beginConsumption(token);
        log.debug("Upload token authorized. uploadId={}, jti={}", context.uploadId(), context.jti());
        return context;
    }

    @Override
    public void markUploadSuccess(UploadAuthorizationContext context) {
        uploadTokenService.completeSuccess(context);
    }

    @Override
    public void markUploadFailure(UploadAuthorizationContext context, String reason) {
        uploadTokenService.completeFailure(context, reason);
    }
}
