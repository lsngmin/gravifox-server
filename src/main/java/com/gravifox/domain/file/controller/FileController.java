package com.gravifox.domain.file.controller;

import com.gravifox.domain.file.exception.UploadException;
import com.gravifox.domain.file.service.FileService;
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
            @Parameter(description = "업로드할 이미지 파일들", required = true)
            @RequestParam("files") MultipartFile[] files) {

        if(files == null || files.length == 0) {
            throw new UploadException("Nofiles to upload");
        }
        for (MultipartFile file : files) {
            checkFileType(file.getOriginalFilename());
        }
        List<String> result = uploadUtil.uplaod(files);
        return ResponseEntity.ok(result);
    }

    private void checkFileType(String fileName) throws UploadException {
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        String regExp = "^(jpg|jpeg|png|gif)";
        if(!suffix.matches(regExp)) {
            throw new UploadException("Invalid file format");
        }
    }
}