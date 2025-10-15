package com.gravifox.domain.analysisreport.domain;

import com.gravifox.domain.analysisreport.domain.converter.AnalysisLabelConverter;
import com.gravifox.domain.analysisreport.domain.converter.AnalysisMediaTypeConverter;
import com.gravifox.domain.member.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "analysis_report",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_analysis_report_user_upload", columnNames = {"user_no", "upload_id"})
        },
        indexes = {
                @Index(name = "idx_analysis_report_user", columnList = "user_no"),
                @Index(name = "idx_analysis_report_user_created_at_desc", columnList = "user_no, created_at DESC"),
                @Index(name = "idx_analysis_report_user_media_type", columnList = "user_no, media_type"),
                @Index(name = "idx_analysis_report_model_version", columnList = "model_version")
        }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_no", nullable = false, foreignKey = @ForeignKey(name = "fk_analysis_report_user"))
    private User user;

    @Column(name = "upload_id", nullable = false, length = 64)
    private String uploadId;

    @Convert(converter = AnalysisMediaTypeConverter.class)
    @Column(name = "media_type", nullable = false, length = 16)
    @Builder.Default
    private AnalysisMediaType mediaType = AnalysisMediaType.IMAGE;

    @Convert(converter = AnalysisLabelConverter.class)
    @Column(name = "label", nullable = false, length = 16)
    private AnalysisLabel label;

    @Column(name = "score", nullable = false, precision = 5, scale = 4)
    private BigDecimal score;

    @Column(name = "model_version", nullable = false, length = 32)
    private String modelVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "heatmap_json", nullable = false, columnDefinition = "json")
    private String heatmapJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta_json", columnDefinition = "json")
    private String metaJson;

    @Column(name = "inference_time_ms")
    private Integer inferenceTimeMs;

    @Column(name = "input_resolution", length = 16)
    private String inputResolution;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static AnalysisReport create(
            User user,
            String uploadId,
            AnalysisMediaType mediaType,
            AnalysisLabel label,
            BigDecimal score,
            String modelVersion,
            String heatmapJson,
            String metaJson,
            Integer inferenceTimeMs,
            String inputResolution
    ) {
        return AnalysisReport.builder()
                .user(user)
                .uploadId(uploadId)
                .mediaType(mediaType)
                .label(label)
                .score(score)
                .modelVersion(modelVersion)
                .heatmapJson(heatmapJson)
                .metaJson(metaJson)
                .inferenceTimeMs(inferenceTimeMs)
                .inputResolution(inputResolution)
                .build();
    }

    public void updateResult(
            AnalysisMediaType mediaType,
            AnalysisLabel label,
            BigDecimal score,
            String modelVersion,
            String heatmapJson,
            String metaJson,
            Integer inferenceTimeMs,
            String inputResolution
    ) {
        this.mediaType = mediaType;
        this.label = label;
        this.score = score;
        this.modelVersion = modelVersion;
        this.heatmapJson = heatmapJson;
        this.metaJson = metaJson;
        this.inferenceTimeMs = inferenceTimeMs;
        this.inputResolution = inputResolution;
    }
}
