package com.gravifox.domain.analysis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "analyze.quota")
public class AnalyzeQuotaProperties {

    /**
     * 월간 기본 이용 한도. (기본값 100회)
     */
    private int monthlyDefault = 100;

    public int getMonthlyDefault() {
        return monthlyDefault;
    }

    public void setMonthlyDefault(int monthlyDefault) {
        this.monthlyDefault = monthlyDefault;
    }
}
