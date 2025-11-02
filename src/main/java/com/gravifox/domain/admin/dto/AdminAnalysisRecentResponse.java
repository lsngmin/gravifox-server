package com.gravifox.domain.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public record AdminAnalysisRecentResponse(
        Long userNo,
        String userId,
        String nickname,
        String uploadId,
        String label,
        String mediaType,
        BigDecimal score,
        String modelVersion,
        Integer inferenceTimeMs,
        LocalDateTime createdAt
) {

    public static AdminAnalysisRecentResponse from(AdminAnalysisRecentQueryResult result) {
        return new AdminAnalysisRecentResponse(
                result.userNo(),
                result.userId(),
                result.nickname(),
                result.uploadId(),
                Optional.ofNullable(result.label()).map(Enum::name).orElse(null),
                Optional.ofNullable(result.mediaType()).map(Enum::name).orElse(null),
                result.score(),
                result.modelVersion(),
                result.inferenceTimeMs(),
                result.createdAt()
        );
    }
}
