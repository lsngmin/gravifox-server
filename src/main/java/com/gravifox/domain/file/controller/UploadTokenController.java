package com.gravifox.domain.file.controller;

import com.gravifox.domain.file.token.UploadTokenService;
import com.gravifox.domain.file.token.dto.UploadTokenIssueRequest;
import com.gravifox.domain.file.token.dto.UploadTokenIssueResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "파일 업로드 토큰", description = "업로드용 JWT 토큰을 발급합니다.")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/files")
public class UploadTokenController {

    private final UploadTokenService uploadTokenService;

    @Operation(summary = "업로드 토큰 발급", description = "사전 등록된 uploadId로 단일 사용 업로드 토큰을 발급합니다.")
    @PostMapping("/upload-token")
    public ResponseEntity<UploadTokenIssueResponse> issueUploadToken(@Valid @RequestBody UploadTokenIssueRequest request) {
        UploadTokenService.UploadTokenIssueResult result = uploadTokenService.issueToken(request.uploadId());
        log.debug("Issued upload token uploadId={}, jti={}", result.uploadId(), result.jti());
        return ResponseEntity.ok(new UploadTokenIssueResponse(result.token(), result.expiresAt(), result.uploadId(), result.jti()));
    }
}
