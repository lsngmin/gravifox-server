package com.gravifox.domain.analysisreport.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "분석 리포트 요약 통계")
public record AnalysisReportSummaryStat(
        @Schema(description = "AI 라벨 건수", example = "120") long aiCount,
        @Schema(description = "REAL 라벨 건수", example = "30") long realCount,
        @Schema(description = "UNKNOWN 라벨 건수", example = "5") long unknownCount,
        @Schema(description = "평균 점수", example = "0.9412") BigDecimal averageScore,
        @Schema(description = "평균 추론 시간(ms)", example = "128.4") Double averageInferenceTimeMs,
        @Schema(description = "가장 최근 분석 시각", example = "2025-01-01T12:34:56") LocalDateTime lastAnalyzedAt
) {
}
