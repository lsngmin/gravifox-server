package com.gravifox.domain.analysis.repository;

import com.gravifox.domain.analysis.domain.AnalyzeMonthlyQuota;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalyzeMonthlyQuotaRepository extends JpaRepository<AnalyzeMonthlyQuota, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AnalyzeMonthlyQuota> findByUserNoAndYearAndMonth(Long userNo, int year, int month);
}
