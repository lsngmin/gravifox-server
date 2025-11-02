package com.gravifox.domain.analysisreport.repository;

import com.gravifox.domain.admin.dto.AdminAnalysisRecentQueryResult;
import com.gravifox.domain.analysisreport.domain.AnalysisMediaType;
import com.gravifox.domain.analysisreport.dto.AnalysisReportDetailResponse;
import com.gravifox.domain.analysisreport.dto.AnalysisReportListItem;
import com.gravifox.domain.analysisreport.dto.AnalysisReportSummaryStat;
import com.gravifox.domain.analysisreport.dto.AnalysisReportVersionStat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AnalysisReportRepositoryCustom {

    Page<AnalysisReportListItem> findPageByUser(Long userNo, AnalysisMediaType mediaType, Pageable pageable);

    Optional<AnalysisReportDetailResponse> findDetailByUserAndUploadId(Long userNo, String uploadId);

    List<AnalysisReportVersionStat> aggregateVersionStats(Long userNo, String modelVersion);

    Optional<AnalysisReportSummaryStat> aggregateSummary(Long userNo, AnalysisMediaType mediaType);

    List<AdminAnalysisRecentQueryResult> findLatestReports(int limit);
}
