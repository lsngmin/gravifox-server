package com.gravifox.domain.admin.repository;

import com.gravifox.domain.admin.dto.AdminUserSummaryQueryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.YearMonth;

public interface AdminUserRepositoryCustom {

    Page<AdminUserSummaryQueryResult> findAdminUserSummaries(String keyword, YearMonth targetMonth, Pageable pageable);
}

