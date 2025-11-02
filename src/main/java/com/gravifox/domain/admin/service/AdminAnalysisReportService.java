package com.gravifox.domain.admin.service;

import com.gravifox.domain.admin.dto.AdminAnalysisRecentResponse;
import com.gravifox.domain.analysisreport.repository.AnalysisReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAnalysisReportService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;

    private final AnalysisReportRepository analysisReportRepository;

    public List<AdminAnalysisRecentResponse> getLatestReports(Integer size) {
        int sanitizedSize = sanitizeLimit(size);
        return analysisReportRepository.findLatestReports(sanitizedSize)
                .stream()
                .map(AdminAnalysisRecentResponse::from)
                .toList();
    }

    private int sanitizeLimit(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(size, MAX_LIMIT);
    }
}
