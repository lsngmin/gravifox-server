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
        if (log.isInfoEnabled()) {
            log.info("[UploadToken] begin consumption tokenId={} uploadId={} jti={} expiresAt={}",
                    context.tokenId(), context.uploadId(), context.jti(), context.expiresAt());
        }
        return context;
    }

    @Override
    public void markUploadSuccess(UploadAuthorizationContext context) {
        uploadTokenService.completeSuccess(context);
        if (log.isInfoEnabled()) {
            log.info("[UploadToken] complete success tokenId={} uploadId={} jti={}",
                    context.tokenId(), context.uploadId(), context.jti());
        }
    }

    @Override
    public void markUploadFailure(UploadAuthorizationContext context, String reason) {
        uploadTokenService.completeFailure(context, reason);
        if (log.isWarnEnabled()) {
            log.warn("[UploadToken] complete failure tokenId={} uploadId={} jti={} reason={}",
                    context.tokenId(), context.uploadId(), context.jti(), reason);
        }
    }
}
