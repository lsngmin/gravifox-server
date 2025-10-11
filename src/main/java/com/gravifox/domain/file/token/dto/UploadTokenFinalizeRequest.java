package com.gravifox.domain.file.token.dto;

import com.gravifox.domain.file.token.UploadAuthorizationContext;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UploadTokenFinalizeRequest(
        @NotNull Long tokenId,
        @NotBlank String uploadId,
        @NotBlank String jti,
        String reason
) {
    public UploadAuthorizationContext toContext() {
        return new UploadAuthorizationContext(tokenId, jti, uploadId, null);
    }
}
