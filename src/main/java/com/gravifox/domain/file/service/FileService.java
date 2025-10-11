package com.gravifox.domain.file.service;

import com.gravifox.domain.file.token.UploadAuthorizationContext;

public interface FileService {

    UploadAuthorizationContext authorizeUpload(String token);

    void markUploadSuccess(UploadAuthorizationContext context);

    void markUploadFailure(UploadAuthorizationContext context, String reason);
}
