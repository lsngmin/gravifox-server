package com.gravifox.domain.file.token.dto;

import java.time.Instant;

public record UploadTokenAuthorizeResponse(Long tokenId, String uploadId, String jti, Instant expiresAt) {
}
