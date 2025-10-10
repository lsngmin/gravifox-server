package com.gravifox.domain.dashboard.service.impl;

import com.gravifox.domain.dashboard.domain.Dashboard;
import com.gravifox.domain.dashboard.dto.DashboardInfoResponse;
import com.gravifox.domain.dashboard.repository.DashboardRepository;
import com.gravifox.domain.dashboard.service.DashboardService;
import com.gravifox.security.jwt.util.JWTUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final DashboardRepository dashboardRepository;
    private final JWTUtil jwtUtil;

    @Override
    public DashboardInfoResponse getDashboardData(Long userNo) {
        Optional<Dashboard> dashboard = dashboardRepository.findDashboardByUserNo(userNo);

        return dashboard
                .map(d -> DashboardInfoResponse.builder().apiKey(d.getApiKey()).apiRequestsLeft(d.getApiRequestsLeft()).build())
                .orElseGet(() -> DashboardInfoResponse.builder().apiKey("빈 문자열").build());
    }

    @Override
    @Transactional
    public String generateDashboardApiKey(Long userNo) {
        String token = jwtUtil.createToken(Map.of("userNo", String.valueOf(userNo)), 1);

        Optional<Dashboard> d = dashboardRepository.findDashboardByUserNo(userNo);
        if(d.isPresent() && d.get().getApiKey().equals("")) {
            dashboardRepository.updateApiKey(userNo, token);
            return token;
        }
        else if(d.isPresent()) {
            return d.get().getApiKey();
        }
        else {
            System.out.println(userNo);
            throw new NoSuchElementException();
        }
    }

}

