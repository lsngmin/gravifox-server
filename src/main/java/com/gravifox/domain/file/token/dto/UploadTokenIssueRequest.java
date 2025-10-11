package com.gravifox.domain.file.token.dto;

import jakarta.validation.constraints.NotBlank;

public record UploadTokenIssueRequest(@NotBlank String uploadId) {
}
