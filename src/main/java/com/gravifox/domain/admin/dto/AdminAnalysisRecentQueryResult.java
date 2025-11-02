package com.gravifox.domain.admin.dto;

import com.gravifox.domain.analysisreport.domain.AnalysisLabel;
import com.gravifox.domain.analysisreport.domain.AnalysisMediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminAnalysisRecentQueryResult(
        Long userNo,
        String userId,
        String nickname,
        String uploadId,
        AnalysisLabel label,
        AnalysisMediaType mediaType,
        BigDecimal score,
        String modelVersion,
        Integer inferenceTimeMs,
        LocalDateTime createdAt
) {
}
