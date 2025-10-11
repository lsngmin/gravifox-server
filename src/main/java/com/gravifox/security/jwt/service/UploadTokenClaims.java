package com.gravifox.security.jwt.service;

import java.time.Instant;

public record UploadTokenClaims(String uploadId, String jti, Instant expiresAt) {
}
