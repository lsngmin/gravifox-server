package com.gravifox.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardInfoResponse {
    private String apiKey;
    private Integer apiRequestsLeft;
}
