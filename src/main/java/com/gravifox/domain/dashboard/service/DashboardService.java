package com.gravifox.domain.dashboard.service;

import com.gravifox.domain.dashboard.dto.DashboardInfoResponse;

public interface DashboardService {
    public DashboardInfoResponse getDashboardData(Long userNo);
    public String generateDashboardApiKey(Long userNo);
}
