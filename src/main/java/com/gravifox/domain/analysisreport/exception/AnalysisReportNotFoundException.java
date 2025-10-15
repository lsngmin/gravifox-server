package com.gravifox.domain.analysisreport.exception;

import com.gravifox.domain.member.exception.common.ErrorCode;
import com.gravifox.exception.GlobalException;

public class AnalysisReportNotFoundException extends GlobalException {

    public AnalysisReportNotFoundException(String uploadId) {
        super(ErrorCode.ANALYSIS_REPORT_NOT_FOUND, uploadId);
    }
}
