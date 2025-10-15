package com.gravifox.domain.analysisreport.controller;

import com.gravifox.domain.analysisreport.domain.AnalysisMediaType;
import com.gravifox.domain.analysisreport.dto.*;
import com.gravifox.domain.analysisreport.service.AnalysisReportService;
import com.gravifox.security.jwt.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analysis/report")
@RequiredArgsConstructor
@Validated
@Tag(name = "분석 리포트", description = "AI 분석 결과 리포트를 조회 및 등록하는 API")
public class AnalysisReportController {

    private final AnalysisReportService analysisReportService;

    @Operation(
            summary = "분석 리포트 목록 조회",
            description = """
                    createdAt 기준 최신순 10건을 기본으로 반환합니다. \
                    heatmap_json 및 meta_json은 제외되어 목록 성능을 유지합니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PageResponse.class)))
            }
    )
    @GetMapping
    public PageResponse<AnalysisReportListItem> getReports(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "미디어 유형 필터", example = "image") @RequestParam(name = "mediaType", required = false) AnalysisMediaType mediaType,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userNo = resolveUserNo(principal);
        return analysisReportService.getReports(userNo, mediaType, pageable);
    }

    @Operation(
            summary = "분석 리포트 상세 조회",
            description = "heatmap_json, meta_json을 포함한 단건 상세를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AnalysisReportDetailResponse.class))),
                    @ApiResponse(responseCode = "404", description = "리포트를 찾을 수 없음",
                            content = @Content(mediaType = "application/json"))
            }
    )
    @GetMapping("/{uploadId}")
    public AnalysisReportDetailResponse getReportDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("uploadId") String uploadId
    ) {
        Long userNo = resolveUserNo(principal);
        return analysisReportService.getReportDetail(userNo, uploadId);
    }

    @Operation(
            summary = "모델 버전별 분석 통계",
            description = "ai/real/unknown 건수, 평균 score, 평균 inference_time_ms를 모델 버전별로 집계합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "집계 성공",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = AnalysisReportVersionStat.class))))
            }
    )
    @GetMapping("/stats/versions")
    public List<AnalysisReportVersionStat> getVersionStats(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "특정 모델 버전 필터", example = "v1.2.0") @RequestParam(name = "modelVersion", required = false) String modelVersion
    ) {
        Long userNo = resolveUserNo(principal);
        return analysisReportService.getVersionStats(userNo, modelVersion);
    }

    @Operation(
            summary = "요약 통계 조회",
            description = "필요시 Redis 등의 외부 캐시로 확장 가능한 기본 요약 통계를 제공합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "집계 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AnalysisReportSummaryStat.class)))
            }
    )
    @GetMapping("/stats/summary")
    public AnalysisReportSummaryStat getSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "미디어 유형 필터", example = "video") @RequestParam(name = "mediaType", required = false) AnalysisMediaType mediaType
    ) {
        Long userNo = resolveUserNo(principal);
        return analysisReportService.getSummary(userNo, mediaType);
    }

    @Operation(
            summary = "분석 리포트 저장/갱신",
            description = "동일 user/upload 조합이 존재하면 UPDATE, 없으면 INSERT로 처리합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "저장 성공"),
                    @ApiResponse(responseCode = "400", description = "유효하지 않은 요청",
                            content = @Content(mediaType = "application/json"))
            }
    )
    @PutMapping("/{uploadId}")
    public ResponseEntity<Void> upsertReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("uploadId") String uploadId,
            @Valid @RequestBody AnalysisReportUpsertRequest request
    ) {
        if (!uploadId.equals(request.uploadId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "uploadId mismatch between path and body");
        }
        Long userNo = resolveUserNo(principal);
        analysisReportService.upsertReport(userNo, request);
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserNo(UserPrincipal principal) {
        if (principal == null || principal.getName() == null) {
            throw new AccessDeniedException("User authentication is required.");
        }
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("Invalid authenticated user identifier.");
        }
    }
}
