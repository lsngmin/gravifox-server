package com.gravifox.domain.admin.controller;

import com.gravifox.domain.admin.dto.AdminAnalysisRecentResponse;
import com.gravifox.domain.admin.service.AdminAnalysisReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/v1/analysis/reports")
@RequiredArgsConstructor
@Tag(name = "관리자 분석 리포트", description = "관리자용 최신 분석 리포트 조회 API")
public class AdminAnalysisReportController {

    private final AdminAnalysisReportService adminAnalysisReportService;

    @Operation(
            summary = "최신 분석 리포트 조회",
            description = "createdAt 기준으로 모든 사용자 리포트 중 최신 순으로 최대 100건까지 조회합니다."
    )
    @GetMapping("/latest")
    public List<AdminAnalysisRecentResponse> getLatestReports(
            @Parameter(description = "가져올 리포트 수", example = "10")
            @RequestParam(name = "size", required = false) Integer size
    ) {
        return adminAnalysisReportService.getLatestReports(size);
    }
}
