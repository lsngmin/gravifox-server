package com.gravifox.domain.analysis.repository;

import com.gravifox.domain.analysis.domain.AnalyzeJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyzeJobRepository extends JpaRepository<AnalyzeJob, String> {
}
