package com.gravifox.domain.analysis.controller;

import com.gravifox.domain.analysis.service.AnalyzeUsageService;
import com.gravifox.security.jwt.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/analyze/quota")
@Tag(name = "Analyze Quota", description = "분석 월간 사용 한도 관련 API")
public class AnalyzeQuotaController {

    private final AnalyzeUsageService analyzeUsageService;

    public AnalyzeQuotaController(AnalyzeUsageService analyzeUsageService) {
        this.analyzeUsageService = analyzeUsageService;
    }

    @Operation(summary = "현재 월간 분석 한도 조회")
    @GetMapping("/summary")
    public ResponseEntity<AnalyzeUsageService.QuotaSnapshot> getQuota(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized");
        }
        Long userNo = Long.parseLong(userPrincipal.getName());
        return ResponseEntity.ok(analyzeUsageService.getQuotaSnapshot(userNo));
    }
}
