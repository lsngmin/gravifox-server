package com.gravifox.domain.analysisreport.dto;

import com.gravifox.domain.analysisreport.domain.AnalysisLabel;
import com.gravifox.domain.analysisreport.domain.AnalysisMediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "분석 리포트 저장/갱신 요청")
public record AnalysisReportUpsertRequest(
        @Schema(description = "업로드 식별자", example = "upload-123abc", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 64)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "uploadId는 영문, 숫자, '.', '_', '-'만 사용할 수 있습니다.")
        String uploadId,

        @Schema(description = "미디어 유형", example = "image", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        AnalysisMediaType mediaType,

        @Schema(description = "분석 라벨", example = "ai", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        AnalysisLabel label,

        @Schema(description = "분석 점수(0.0~1.0)", example = "0.9821", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        @DecimalMax(value = "1.0", inclusive = true)
        BigDecimal score,

        @Schema(description = "모델 버전", example = "v1.2.0", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 32)
        String modelVersion,

        @Schema(description = "히트맵 JSON(raw)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String heatmapJson,

        @Schema(description = "추가 메타정보 JSON", nullable = true)
        String metaJson,

        @Schema(description = "추론 시간(ms)", nullable = true, example = "124")
        @PositiveOrZero
        @Max(3600000)
        Integer inferenceTimeMs,

        @Schema(description = "모델 입력 해상도", nullable = true, example = "512x512")
        @Size(max = 16)
        String inputResolution
) {
}
