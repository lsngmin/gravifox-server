package com.gravifox.domain.analysisreport.service;

import com.gravifox.domain.analysisreport.domain.AnalysisMediaType;
import com.gravifox.domain.analysisreport.domain.AnalysisReport;
import com.gravifox.domain.analysisreport.dto.*;
import com.gravifox.domain.analysisreport.exception.AnalysisReportNotFoundException;
import com.gravifox.domain.analysisreport.repository.AnalysisReportRepository;
import com.gravifox.domain.member.domain.user.User;
import com.gravifox.domain.member.exception.user.UserNotFoundException;
import com.gravifox.domain.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisReportService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final AnalysisReportRepository analysisReportRepository;
    private final UserRepository userRepository;

    public PageResponse<AnalysisReportListItem> getReports(Long userNo, AnalysisMediaType mediaType, Pageable pageable) {
        Pageable sanitized = sanitizePageable(pageable);
        Page<AnalysisReportListItem> page = analysisReportRepository.findPageByUser(userNo, mediaType, sanitized);
        return PageResponse.from(page);
    }

    public AnalysisReportDetailResponse getReportDetail(Long userNo, String uploadId) {
        return analysisReportRepository.findDetailByUserAndUploadId(userNo, uploadId)
                .orElseThrow(() -> new AnalysisReportNotFoundException(uploadId));
    }

    public List<AnalysisReportVersionStat> getVersionStats(Long userNo, String modelVersion) {
        return analysisReportRepository.aggregateVersionStats(userNo, StringUtils.hasText(modelVersion) ? modelVersion : null);
    }

    public AnalysisReportSummaryStat getSummary(Long userNo, AnalysisMediaType mediaType) {
        return analysisReportRepository.aggregateSummary(userNo, mediaType)
                .orElseGet(() -> new AnalysisReportSummaryStat(0, 0, 0, null, null, null));
    }

    @Transactional
    public void upsertReport(Long userNo, AnalysisReportUpsertRequest request) {
        User user = userRepository.findById(userNo).orElseThrow(() -> new UserNotFoundException(userNo));
        BigDecimal normalizedScore = normalizeScore(request.score());

        analysisReportRepository.findByUserUserNoAndUploadId(userNo, request.uploadId())
                .ifPresentOrElse(existing -> {
                    existing.updateResult(
                            request.mediaType(),
                            request.label(),
                            normalizedScore,
                            request.modelVersion(),
                            request.heatmapJson(),
                            emptyToNull(request.metaJson()),
                            request.inferenceTimeMs(),
                            emptyToNull(request.inputResolution())
                    );
                    log.debug("Updated analysis report for userNo={}, uploadId={}", userNo, request.uploadId());
                }, () -> {
                    AnalysisReport created = AnalysisReport.create(
                            user,
                            request.uploadId(),
                            request.mediaType(),
                            request.label(),
                            normalizedScore,
                            request.modelVersion(),
                            request.heatmapJson(),
                            emptyToNull(request.metaJson()),
                            request.inferenceTimeMs(),
                            emptyToNull(request.inputResolution())
                    );
                    analysisReportRepository.save(created);
                    log.debug("Created analysis report for userNo={}, uploadId={}", userNo, request.uploadId());
                });
    }

    private Pageable sanitizePageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by(Sort.Order.desc("createdAt")));
        }
        int pageSize = pageable.getPageSize() <= 0 ? DEFAULT_PAGE_SIZE : pageable.getPageSize();
        int pageNumber = Math.max(pageable.getPageNumber(), 0);
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Order.desc("createdAt"));
        return PageRequest.of(pageNumber, pageSize, sort);
    }

    private BigDecimal normalizeScore(BigDecimal score) {
        if (score == null) {
            return null;
        }
        return score.setScale(4, RoundingMode.HALF_UP);
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }
}
