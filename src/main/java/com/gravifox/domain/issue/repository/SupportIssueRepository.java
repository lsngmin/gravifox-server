package com.gravifox.domain.issue.repository;

import com.gravifox.domain.issue.domain.SupportIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportIssueRepository extends JpaRepository<SupportIssue, Long> {
}

