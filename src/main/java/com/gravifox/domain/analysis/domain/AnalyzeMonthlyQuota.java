package com.gravifox.domain.analysis.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "analyze_monthly_quota", schema = "member",
        uniqueConstraints = @UniqueConstraint(name = "uk_analyze_quota_user_period", columnNames = {"user_no", "year", "month"}))
public class AnalyzeMonthlyQuota {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quota_id", nullable = false, updatable = false)
    private Long quotaId;

    @Column(name = "user_no", nullable = false)
    private Long userNo;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "month", nullable = false)
    private int month;

    @Column(name = "limit_count", nullable = false)
    private int limit;

    @Column(name = "used_count", nullable = false)
    private int usedCount;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected AnalyzeMonthlyQuota() {
    }

    public AnalyzeMonthlyQuota(Long userNo, int year, int month, int limit, LocalDate periodStart) {
        this.userNo = userNo;
        this.year = year;
        this.month = month;
        this.limit = limit;
        this.periodStart = periodStart;
        this.usedCount = 0;
        this.updatedAt = LocalDateTime.now(KST);
    }

    public Long getQuotaId() {
        return quotaId;
    }

    public Long getUserNo() {
        return userNo;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getLimit() {
        return limit;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setLimit(int limit) {
        this.limit = limit;
        touch();
    }

    public void incrementUsed() {
        this.usedCount += 1;
        touch();
    }

    public void touch() {
        this.updatedAt = LocalDateTime.now(KST);
    }

    public ZonedDateTime getPeriodStartZoned(ZoneId zoneId) {
        return this.periodStart.atStartOfDay(zoneId);
    }
}
