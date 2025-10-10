package com.gravifox.domain.file.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class UploadUtil {
    @Value("${org.zerock.upload.path}")
    private String uploadPath;

    @PostConstruct
    public void init() {
        File tmpDir = new File(uploadPath);
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
        uploadPath = tmpDir.getAbsolutePath();
        log.info("Upload path: {} " ,uploadPath);
    }
    public List<String> uplaod(MultipartFile[] files) {
        List<String> result = new ArrayList<>();
        for (MultipartFile file : files) {
            log.info("Uploading file: " + file.getOriginalFilename());
            if(!file.getContentType().startsWith("image")) {
                log.error("File is not an image : {}", file.getContentType());
                continue;
            }
            String uuid = UUID.randomUUID().toString();
            String saveFileName = uuid + "_" + file.getOriginalFilename();
            try (InputStream in = file.getInputStream();
                 OutputStream out = new FileOutputStream(uploadPath + File.separator +saveFileName)
            ) {
                FileCopyUtils.copy(in, out);
                result.add(saveFileName);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return result;
    }
}
