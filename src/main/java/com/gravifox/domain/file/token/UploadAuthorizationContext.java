package com.gravifox.domain.file.token;

import java.time.Instant;

public record UploadAuthorizationContext(Long tokenId, String jti, String uploadId, Instant expiresAt) {
}
