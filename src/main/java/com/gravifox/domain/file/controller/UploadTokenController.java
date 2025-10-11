package com.gravifox.domain.file.controller;

import com.gravifox.domain.file.exception.UploadTokenUnauthorizedException;
import com.gravifox.domain.file.service.FileService;
import com.gravifox.domain.file.token.UploadAuthorizationContext;
import com.gravifox.domain.file.token.UploadTokenService;
import com.gravifox.domain.file.token.dto.UploadTokenIssueRequest;
import com.gravifox.domain.file.token.dto.UploadTokenIssueResponse;
import com.gravifox.domain.file.token.dto.UploadTokenAuthorizeResponse;
import com.gravifox.domain.file.token.dto.UploadTokenFinalizeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "파일 업로드 토큰", description = "업로드용 JWT 토큰을 발급합니다.")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/files")
public class UploadTokenController {

    private final UploadTokenService uploadTokenService;
    private final FileService fileService;
    @Value("${upload.token.service-key:}")
    private String serviceKey;
    @Value("${upload.token.disabled:false}")
    private boolean uploadTokenDisabled;
    @Value("${upload.token.disabled-token:}")
    private String disabledToken;
    @Value("${upload.token.disabled-ttl-seconds:300}")
    private long disabledTtlSeconds;

    @Operation(summary = "업로드 토큰 발급", description = "사전 등록된 uploadId로 단일 사용 업로드 토큰을 발급합니다.")
    @PostMapping("/upload-token")
    public ResponseEntity<UploadTokenIssueResponse> issueUploadToken(@Valid @RequestBody UploadTokenIssueRequest request) {
        if (uploadTokenDisabled) {
            var token = resolveDisabledToken();
            var expiresAt = java.time.Instant.now().plusSeconds(Math.max(1L, disabledTtlSeconds));
            return ResponseEntity.ok(new UploadTokenIssueResponse(token, expiresAt, request.uploadId(), "disabled"));
        }
        UploadTokenService.UploadTokenIssueResult result = uploadTokenService.issueToken(request.uploadId());
        log.debug("Issued upload token uploadId={}, jti={}", result.uploadId(), result.jti());
        return ResponseEntity.ok(new UploadTokenIssueResponse(result.token(), result.expiresAt(), result.uploadId(), result.jti()));
    }

    @Operation(summary = "업로드 토큰 인가", description = "업로드 서버가 업로드 토큰을 검증하고 사용 상태로 전환합니다.")
    @PostMapping("/upload-token/authorize")
    public ResponseEntity<UploadTokenAuthorizeResponse> authorizeUploadToken(
            @RequestHeader(value = "X-Service-Key", required = false) String internalServiceKey,
            @RequestHeader(value = "Upload-Token", required = false) String uploadToken,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        verifyServiceKey(internalServiceKey);
        if (uploadTokenDisabled) {
            return ResponseEntity.ok(new UploadTokenAuthorizeResponse(
                    -1L,
                    null,
                    "disabled",
                    java.time.Instant.now().plusSeconds(Math.max(1L, disabledTtlSeconds))
            ));
        }
        String tokenValue = resolveUploadToken(authorizationHeader, uploadToken);
        UploadAuthorizationContext context = fileService.authorizeUpload(tokenValue);
        UploadTokenAuthorizeResponse response = new UploadTokenAuthorizeResponse(
                context.tokenId(),
                context.uploadId(),
                context.jti(),
                context.expiresAt()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "업로드 토큰 성공 처리", description = "업로드가 성공적으로 완료되면 토큰을 소비 상태로 전환합니다.")
    @PostMapping("/upload-token/success")
    public ResponseEntity<Void> markUploadSuccess(
            @RequestHeader(value = "X-Service-Key", required = false) String internalServiceKey,
            @Valid @RequestBody UploadTokenFinalizeRequest request) {
        verifyServiceKey(internalServiceKey);
        if (uploadTokenDisabled) {
            return ResponseEntity.ok().build();
        }
        UploadAuthorizationContext context = request.toContext();
        fileService.markUploadSuccess(context);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "업로드 토큰 실패 처리", description = "업로드가 실패하면 토큰을 실패 상태로 전환합니다.")
    @PostMapping("/upload-token/failure")
    public ResponseEntity<Void> markUploadFailure(
            @RequestHeader(value = "X-Service-Key", required = false) String internalServiceKey,
            @Valid @RequestBody UploadTokenFinalizeRequest request) {
        verifyServiceKey(internalServiceKey);
        if (uploadTokenDisabled) {
            return ResponseEntity.ok().build();
        }
        UploadAuthorizationContext context = request.toContext();
        fileService.markUploadFailure(context, request.reason());
        return ResponseEntity.ok().build();
    }

    private String resolveUploadToken(String authorization, String uploadToken) {
        if (uploadToken != null && !uploadToken.isBlank()) {
            return uploadToken.trim();
        }
        if (authorization != null) {
            String value = authorization.trim();
            if (value.regionMatches(true, 0, "Bearer ", 0, 7)) {
                String token = value.substring(7).trim();
                if (!token.isEmpty()) {
                    return token;
                }
            }
        }
        throw new UploadTokenUnauthorizedException("업로드 토큰이 필요해요.");
    }

    private void verifyServiceKey(String providedKey) {
        if (!StringUtils.hasText(serviceKey)) {
            return;
        }
        if (uploadTokenDisabled) {
            return;
        }
        if (!StringUtils.hasText(providedKey) || !serviceKey.equals(providedKey.trim())) {
            throw new UploadTokenUnauthorizedException("업로드 토큰 호출 권한이 없어요.");
        }
    }

    private String resolveDisabledToken() {
        if (StringUtils.hasText(disabledToken)) {
            return disabledToken.trim();
        }
        return "dev-upload-token";
    }
}
