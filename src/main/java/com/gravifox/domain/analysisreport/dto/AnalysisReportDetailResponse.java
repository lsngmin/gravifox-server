package com.gravifox.domain.analysisreport.dto;

import com.gravifox.domain.analysisreport.domain.AnalysisLabel;
import com.gravifox.domain.analysisreport.domain.AnalysisMediaType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "분석 리포트 상세 응답")
public record AnalysisReportDetailResponse(
        @Schema(description = "업로드 식별자", example = "upload-123") String uploadId,
        @Schema(description = "분석 라벨", example = "ai") AnalysisLabel label,
        @Schema(description = "분석 점수(0.0~1.0)", example = "0.9821") BigDecimal score,
        @Schema(description = "모델 버전", example = "v1.2.0") String modelVersion,
        @Schema(description = "미디어 유형", example = "image") AnalysisMediaType mediaType,
        @Schema(description = "모델 입력 해상도", example = "512x512") String inputResolution,
        @Schema(description = "추론 시간(ms)", example = "124") Integer inferenceTimeMs,
        @Schema(description = "생성 시각", example = "2025-01-01T12:34:56") LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2025-01-01T12:40:00") LocalDateTime updatedAt,
        @Schema(description = "히트맵 RAW JSON") String heatmapJson,
        @Schema(description = "부가 정보 JSON") String metaJson
) {
}
