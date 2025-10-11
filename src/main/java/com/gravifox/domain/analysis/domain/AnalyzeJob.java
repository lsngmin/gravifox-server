package com.gravifox.domain.analysis.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "analyze_job", schema = "member")
public class AnalyzeJob {

    @Id
    @Column(name = "job_id", nullable = false, updatable = false, length = 26)
    private String jobId;

    @Column(name = "user_no", nullable = false)
    private Long userNo;

    @Column(name = "upload_id", nullable = false, length = 128)
    private String uploadId;

    @Column(name = "model_key", length = 128)
    private String modelKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    private AnalyzeJobStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    protected AnalyzeJob() {
    }

    public AnalyzeJob(String jobId, Long userNo, String uploadId, String modelKey, LocalDateTime createdAt) {
        this.jobId = jobId;
        this.userNo = userNo;
        this.uploadId = uploadId;
        this.modelKey = modelKey;
        this.createdAt = createdAt;
        this.status = AnalyzeJobStatus.PENDING;
    }

    public String getJobId() {
        return jobId;
    }

    public Long getUserNo() {
        return userNo;
    }

    public String getUploadId() {
        return uploadId;
    }

    public String getModelKey() {
        return modelKey;
    }

    public AnalyzeJobStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void markCompleted(LocalDateTime completedAt) {
        this.status = AnalyzeJobStatus.COMPLETED;
        this.completedAt = completedAt;
    }

    public void markFailed() {
        this.status = AnalyzeJobStatus.FAILED;
    }
}
