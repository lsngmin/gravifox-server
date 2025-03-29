package com.tvb.api.domain.upload.controller;

import com.tvb.api.domain.upload.exception.UploadException;
import com.tvb.api.domain.upload.service.FileService;
import com.tvb.api.domain.upload.util.UploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/v1/files")
public class FileController {
    @Autowired
    private  UploadUtil uploadUtil;
    @Autowired
    private  FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("files") MultipartFile[] files) {
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