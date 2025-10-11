package com.gravifox.domain.analysis.service;

import com.gravifox.domain.analysis.config.AnalyzeQuotaProperties;
import com.gravifox.domain.analysis.domain.AnalyzeJob;
import com.gravifox.domain.analysis.domain.AnalyzeJobStatus;
import com.gravifox.domain.analysis.domain.AnalyzeMonthlyQuota;
import com.gravifox.domain.analysis.repository.AnalyzeJobRepository;
import com.gravifox.domain.analysis.repository.AnalyzeMonthlyQuotaRepository;
import com.gravifox.domain.member.domain.user.LoginType;
import com.gravifox.domain.member.domain.user.User;
import com.gravifox.domain.member.exception.user.UserNotFoundException;
import com.gravifox.domain.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzeUsageService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final AnalyzeMonthlyQuotaRepository quotaRepository;
    private final AnalyzeJobRepository jobRepository;
    private final UserRepository userRepository;
    private final AnalyzeQuotaProperties quotaProperties;

    @Transactional
    public QuotaSnapshot prepareNewJob(Long userNo, String jobId, String uploadId, String modelKey) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new UserNotFoundException(userNo));

        if (user.getLoginType() == LoginType.EMAIL && !Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "email_not_verified");
        }

        YearMonth currentMonth = YearMonth.now(KST);
        AnalyzeMonthlyQuota quota = ensureQuotaRow(userNo, currentMonth);

        if (quota.getUsedCount() >= quota.getLimit()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "quota_exhausted");
        }

        if (jobRepository.existsById(jobId)) {
            return buildSnapshot(user, quota);
        }

        AnalyzeJob job = new AnalyzeJob(jobId, userNo, uploadId, modelKey, now());
        jobRepository.save(job);

        return buildSnapshot(user, quota);
    }

    @Transactional
    public QuotaSnapshot getQuotaSnapshot(Long userNo) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new UserNotFoundException(userNo));

        YearMonth currentMonth = YearMonth.now(KST);
        AnalyzeMonthlyQuota quota = ensureQuotaRow(userNo, currentMonth);

        return buildSnapshot(user, quota);
    }

    @Transactional
    public void markJobCompleted(String jobId) {
        Optional<AnalyzeJob> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            log.debug("[Quota] Job not found for completion jobId={}", jobId);
            return;
        }

        AnalyzeJob job = jobOpt.get();
        if (job.getStatus() == AnalyzeJobStatus.COMPLETED) {
            return;
        }
        if (job.getStatus() == AnalyzeJobStatus.FAILED) {
            // 이미 실패 처리된 경우에는 사용량을 차감하지 않는다.
            return;
        }

        LocalDateTime completedAt = now();
        job.markCompleted(completedAt);

        YearMonth completionMonth = YearMonth.from(completedAt.atZone(KST));
        AnalyzeMonthlyQuota quota = ensureQuotaRow(job.getUserNo(), completionMonth);
        quota.incrementUsed();
        quotaRepository.save(quota);
    }

    @Transactional
    public void markJobFailed(String jobId) {
        jobRepository.findById(jobId).ifPresent(job -> {
            if (job.getStatus() == AnalyzeJobStatus.COMPLETED) {
                return;
            }
            job.markFailed();
        });
    }

    private AnalyzeMonthlyQuota ensureQuotaRow(Long userNo, YearMonth yearMonth) {
        return quotaRepository.findByUserNoAndYearAndMonth(userNo, yearMonth.getYear(), yearMonth.getMonthValue())
                .orElseGet(() -> quotaRepository.save(
                        new AnalyzeMonthlyQuota(
                                userNo,
                                yearMonth.getYear(),
                                yearMonth.getMonthValue(),
                                quotaProperties.getMonthlyDefault(),
                                yearMonth.atDay(1)
                        )
                ));
    }

    private QuotaSnapshot buildSnapshot(User user, AnalyzeMonthlyQuota quota) {
        ZonedDateTime periodStart = quota.getPeriodStartZoned(KST);
        ZonedDateTime periodEnd = periodStart.plusMonths(1);
        int remaining = Math.max(0, quota.getLimit() - quota.getUsedCount());
        return new QuotaSnapshot(
                quota.getLimit(),
                quota.getUsedCount(),
                remaining,
                periodStart,
                periodEnd,
                user.getLoginType(),
                Boolean.TRUE.equals(user.getEmailVerified())
        );
    }

    private LocalDateTime now() {
        return LocalDateTime.now(KST);
    }

    public record QuotaSnapshot(
            int limit,
            int used,
            int remaining,
            ZonedDateTime periodStart,
            ZonedDateTime periodEnd,
            LoginType loginType,
            boolean emailVerified
    ) {
    }
}
