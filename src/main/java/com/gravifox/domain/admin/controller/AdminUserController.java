package com.gravifox.domain.admin.controller;

import com.gravifox.domain.admin.dto.AdminUserSummaryResponse;
import com.gravifox.domain.admin.service.AdminUserService;
import com.gravifox.domain.analysisreport.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/v1/users")
@RequiredArgsConstructor
@Tag(name = "관리자 사용자", description = "관리자용 사용자 계정 현황 API")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "관리자 사용자 목록 조회")
    @GetMapping
    public PageResponse<AdminUserSummaryResponse> getAdminUsers(
            @Parameter(description = "검색 키워드 (userId, nickname)") @RequestParam(name = "keyword", required = false) String keyword,
            @ParameterObject @PageableDefault(size = 20, sort = "userNo", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return adminUserService.getUserSummaries(keyword, pageable);
    }

    @Operation(summary = "월간 사용량 초기화")
    @PostMapping("/{userNo}/usage/reset")
    public ResponseEntity<Void> resetMonthlyUsage(@PathVariable("userNo") Long userNo) {
        adminUserService.resetMonthlyUsage(userNo);
        return ResponseEntity.noContent().build();
    }
}
