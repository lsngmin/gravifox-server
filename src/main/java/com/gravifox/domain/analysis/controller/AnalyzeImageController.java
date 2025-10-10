package com.gravifox.domain.analysis.controller;

import com.gravifox.domain.analysis.service.WebClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/images")
@Tag(name = "이미지 분석", description = "딥러닝 서버와 연동하여 이미지 분석 결과를 반환하는 API입니다.")
public class AnalyzeImageController {

    @Autowired
    private WebClientService webClientService;

    @Operation(
            summary = "이미지 분석 요청",
            description = "UUID로 식별되는 이미지를 딥러닝 서버로 전송하여 분석 결과를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "이미지 분석 성공"),
                    @ApiResponse(responseCode = "500", description = "서버 오류 또는 분석 실패")
            }
    )

    @PostMapping("/{uuid}/analyze")
    public ResponseEntity<?> analyzeImage(
            @Parameter(description = "이미지를 식별하는 UUID", example = "fb7a1c25-13be-47cb-a13c-1b7a3ee651f4", required = true)
            @PathVariable("uuid") String uuid
    ) {
        log.info("AnalyzeImageController: analyzeImage: {}", uuid);
        String analyzeResult = webClientService.sendImageToAIServer(uuid);
        log.info("{}", analyzeResult);
        try {
            // 응답 지연 - 3초
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 인터럽트 처리
        }

        return ResponseEntity.ok().body(analyzeResult);
    }

    @Operation(
            summary = "FastAPI 데이터 전송",
            description = "FastAPI 서버에 데이터를 전송합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "요청 성공"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @PostMapping("/send_data")
    public ResponseEntity<?> sendDataToFastAPI() throws IOException {
        String uuid = UUID.randomUUID().toString();
        log.info("AnalyzeImageController: analyzeImage: {}", uuid);

        String analyzeResult = webClientService.sendVideoToAIServer("sample");
        log.info("{}", analyzeResult);
        return ResponseEntity.ok().body(analyzeResult);
    }
}
