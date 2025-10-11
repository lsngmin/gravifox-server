package com.gravifox.domain.file.controller;

import com.gravifox.domain.file.exception.UploadException;
import com.gravifox.domain.file.exception.UploadTokenUnauthorizedException;
import com.gravifox.domain.file.service.FileService;
import com.gravifox.domain.file.token.UploadAuthorizationContext;
import com.gravifox.domain.file.util.UploadUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "파일 업로드", description = "이미지 파일 업로드를 처리합니다.")
@RestController
@Slf4j
@RequestMapping("api/v1/files")
public class FileController {
    @Autowired
    private  UploadUtil uploadUtil;
    @Autowired
    private  FileService fileService;

    @Operation(
            summary = "파일 업로드",
            description = "이미지 파일(jpg, jpeg, png, gif)을 업로드할 수 있습니다. 유효하지 않은 파일 형식은 업로드되지 않습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "업로드 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "업로드할 파일이 없거나 파일 형식이 잘못됨", content = @Content(mediaType = "application/json"))
            }
    )
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @Parameter(description = "업로드 인증 토큰", required = false)
            @RequestHeader(value = "Upload-Token", required = false) String uploadToken,
            @Parameter(description = "Bearer 형식의 업로드 인증 토큰", required = false)
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Parameter(description = "업로드할 이미지 파일들", required = true)
            @RequestParam("files") MultipartFile[] files) {

        UploadAuthorizationContext context = null;
        try {
            String tokenValue = resolveUploadToken(authorization, uploadToken);
            context = fileService.authorizeUpload(tokenValue);

            if(files == null || files.length == 0) {
                throw new UploadException("Nofiles to upload");
            }
            for (MultipartFile file : files) {
                checkFileType(file.getOriginalFilename());
            }
            List<String> result = uploadUtil.uplaod(files);
            fileService.markUploadSuccess(context);
            return ResponseEntity.ok(result);
        } catch (RuntimeException ex) {
            fileService.markUploadFailure(context, ex.getMessage());
            throw ex;
        }
    }

    private void checkFileType(String fileName) throws UploadException {
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        String regExp = "^(jpg|jpeg|png|gif)";
        if(!suffix.matches(regExp)) {
            throw new UploadException("Invalid file format");
        }
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
}
