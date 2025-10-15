package com.gravifox.domain.analysisreport.repository;

import com.gravifox.domain.analysisreport.domain.AnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, Long>, AnalysisReportRepositoryCustom {

    Optional<AnalysisReport> findByUserUserNoAndUploadId(Long userNo, String uploadId);
}
