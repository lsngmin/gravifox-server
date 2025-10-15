package com.gravifox.domain.analysisreport.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "모델 버전별 분석 통계")
public record AnalysisReportVersionStat(
        @Schema(description = "모델 버전", example = "v1.2.0") String modelVersion,
        @Schema(description = "AI 라벨 건수", example = "120") long aiCount,
        @Schema(description = "REAL 라벨 건수", example = "30") long realCount,
        @Schema(description = "UNKNOWN 라벨 건수", example = "5") long unknownCount,
        @Schema(description = "평균 점수", example = "0.9412") BigDecimal averageScore,
        @Schema(description = "평균 추론 시간(ms)", example = "128.4") Double averageInferenceTimeMs
) {
}
