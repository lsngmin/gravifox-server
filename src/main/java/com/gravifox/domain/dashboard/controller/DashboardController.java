package com.gravifox.domain.dashboard.controller;

import com.gravifox.domain.dashboard.dto.DashboardInfoResponse;
import com.gravifox.domain.dashboard.service.DashboardService;
import com.gravifox.security.jwt.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    @Autowired
    private final DashboardService dashboardService;

    @GetMapping("/")
    public ResponseEntity<?> index(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userNo = Long.parseLong(userPrincipal.getName());
        DashboardInfoResponse d = dashboardService.getDashboardData(userNo);
        return ResponseEntity.ok().body(d);
    }
    @PostMapping("/generate")
    public ResponseEntity<?> GenerateToken(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userNo = Long.parseLong(userPrincipal.getName());
        String toekn = dashboardService.generateDashboardApiKey(userNo);
        return ResponseEntity.ok().body(toekn);
    }
}
