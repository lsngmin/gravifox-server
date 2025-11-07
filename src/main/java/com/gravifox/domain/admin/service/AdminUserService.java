package com.gravifox.domain.admin.service;

import com.gravifox.domain.admin.dto.AdminUserSummaryQueryResult;
import com.gravifox.domain.admin.dto.AdminUserSummaryResponse;
import com.gravifox.domain.analysis.config.AnalyzeQuotaProperties;
import com.gravifox.domain.analysis.domain.AnalyzeMonthlyQuota;
import com.gravifox.domain.analysis.repository.AnalyzeMonthlyQuotaRepository;
import com.gravifox.domain.member.repository.UserRepository;
import com.gravifox.domain.analysisreport.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final UserRepository userRepository;
    private final AnalyzeMonthlyQuotaRepository analyzeMonthlyQuotaRepository;
    private final AnalyzeQuotaProperties analyzeQuotaProperties;

    public PageResponse<AdminUserSummaryResponse> getUserSummaries(String keyword, Pageable pageable) {
        Pageable sanitized = sanitizePageable(pageable);
        YearMonth currentMonth = YearMonth.now(KST);

        Page<AdminUserSummaryQueryResult> page = userRepository.findAdminUserSummaries(keyword, currentMonth, sanitized);
        int defaultMonthlyLimit = analyzeQuotaProperties.getMonthlyDefault();
        Page<AdminUserSummaryResponse> mapped = page.map(result -> AdminUserSummaryResponse.from(result, defaultMonthlyLimit));

        return PageResponse.from(mapped);
    }

    @Transactional
    public void resetMonthlyUsage(Long userNo) {
        YearMonth currentMonth = YearMonth.now(KST);
        analyzeMonthlyQuotaRepository.findByUserNoAndYearAndMonth(
                        userNo,
                        currentMonth.getYear(),
                        currentMonth.getMonthValue()
                )
                .ifPresent(AnalyzeMonthlyQuota::resetUsage);
    }

    private Pageable sanitizePageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by(Sort.Order.desc("userNo")));
        }

        int pageSize = pageable.getPageSize() <= 0 ? DEFAULT_PAGE_SIZE : pageable.getPageSize();
        int pageNumber = Math.max(pageable.getPageNumber(), 0);
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Order.desc("userNo"));
        return PageRequest.of(pageNumber, pageSize, sort);
    }
}
