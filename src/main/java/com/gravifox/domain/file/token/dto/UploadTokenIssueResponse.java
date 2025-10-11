package com.gravifox.domain.file.token.dto;

import java.time.Instant;

public record UploadTokenIssueResponse(String uploadToken, Instant expiresAt, String uploadId, String jti) {
}
