package com.gravifox.domain.analysis.service.impl;

import com.gravifox.domain.analysis.service.WebClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class WebClientServiceImpl implements WebClientService {
    @Value("${backend.upload.path}") private String uploadPath;
    @Value("${ai.base.url}") private String fastApiUrl;

    private final WebClient webClient = WebClient.builder()
            .baseUrl(fastApiUrl)
            .build();

    @Override
    public String sendVideoToAIServer(String uuid) {
        // 1) 파일 찾기 (확장자 허용)
        Path filePath = findFilePathOrThrow(uuid); // 예: uuid, uuid.mp4 둘 다 탐색하도록 구현 권장

        // 2) 스트리밍 리소스 사용 (메모리 전체 적재 X)
        FileSystemResource video = new FileSystemResource(filePath);

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder
                .part("file", video)
                .filename(ensureFilenameWithExt(uuid, filePath)) // uuid.mp4 형태 권장
                .contentType(MediaType.valueOf("video/mp4"));

        WebClient client = WebClient.builder()
                .baseUrl(fastApiUrl) // 주입된 Builder 사용
                .build();

        // 3) FastAPI가 파일을 받는 엔드포인트여야 함: /predeict/video/ 가 UploadFile 받도록 복구 필요
        String analyzeResult = client.post()
                .uri("/predeict/video/") // FastAPI 라우트 이름 오타(predeict) 유지 중이면 동일하게
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(bodyBuilder.build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("AI server response: {}", analyzeResult);
        return analyzeResult;
    }

    /** 파일명 보정: 확장자 없으면 원본 파일 확장자 붙여줌 */
    private String ensureFilenameWithExt(String uuid, Path filePath) {
        String name = filePath.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            return uuid + name.substring(dot); // uuid + ".mp4"
        }
        return uuid; // 확장자 없으면 그대로
    }

    /** 파일 탐색: uuid 또는 uuid.* 매칭 */
    private Path findFilePathOrThrow(String uuid) {
        Path dir = Paths.get(uploadPath);
        try (Stream<Path> s = Files.list(dir)) {
            return s.filter(Files::isRegularFile)
                    .filter(p -> {
                        String fn = p.getFileName().toString();
                        return fn.equals(uuid) || fn.startsWith(uuid + ".");
                    })
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "File not found in uploadPath for uuid=" + uuid + ", path=" + dir));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to list upload dir: " + dir, e);
        }
    }

    @Override
    public String sendImageToAIServer(String uuid) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        try {
            InputStream is = Files.newInputStream(findFilePath(uuid));
            byte[] imageBytes = is.readAllBytes();
            ByteArrayResource resource = new ByteArrayResource(imageBytes);
            bodyBuilder.part("file", resource)
                    .filename(uuid)
                    .contentType(MediaType.IMAGE_JPEG);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("{}", fastApiUrl);
        WebClient webClient = WebClient.builder()
                .baseUrl(fastApiUrl)
                .build();

        String analyzeResult = webClient.post()
                .uri("/upload/")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(bodyBuilder.build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.info("{}", analyzeResult);
        return analyzeResult;
    }
    private Path findFilePath(String uuid) {
        Path dirPath = Paths.get(uploadPath);
        try (Stream<Path> stream = Files.list(dirPath)) {
            Optional<Path> foundPath = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(uuid))
                    .findFirst();
            Path filePath = null;
            if (foundPath.isPresent()) {
                filePath = foundPath.get();
                log.info("{}", filePath);
                return filePath;
            }
            //TODO Need Throw Exception
            log.info("Failed to find file path {}", dirPath);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("Failed to find file path2 {}", dirPath);
        return null;
    }
}
