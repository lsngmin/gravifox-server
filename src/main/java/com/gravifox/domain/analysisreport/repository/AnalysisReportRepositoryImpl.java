package com.gravifox.domain.analysisreport.repository;

import com.gravifox.domain.analysisreport.domain.AnalysisLabel;
import com.gravifox.domain.analysisreport.domain.AnalysisMediaType;
import com.gravifox.domain.analysisreport.domain.QAnalysisReport;
import com.gravifox.domain.analysisreport.dto.AnalysisReportDetailResponse;
import com.gravifox.domain.analysisreport.dto.AnalysisReportListItem;
import com.gravifox.domain.analysisreport.dto.AnalysisReportSummaryStat;
import com.gravifox.domain.analysisreport.dto.AnalysisReportVersionStat;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnalysisReportRepositoryImpl implements AnalysisReportRepositoryCustom {

    private static final QAnalysisReport report = QAnalysisReport.analysisReport;

    private final JPAQueryFactory queryFactory;

    public AnalysisReportRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<AnalysisReportListItem> findPageByUser(Long userNo, AnalysisMediaType mediaType, Pageable pageable) {
        Pageable sanitized = applyDefaultSort(pageable);
        BooleanBuilder predicate = new BooleanBuilder().and(report.user.userNo.eq(userNo));
        if (mediaType != null) {
            predicate.and(report.mediaType.eq(mediaType));
        }

        long total = Optional.ofNullable(queryFactory
                        .select(report.count())
                        .from(report)
                        .where(predicate)
                        .fetchOne())
                .orElse(0L);

        if (total == 0) {
            return new PageImpl<>(Collections.emptyList(), sanitized, 0);
        }

        List<OrderSpecifier<?>> orderSpecifiers = resolveSort(sanitized.getSort());
        if (orderSpecifiers.isEmpty()) {
            orderSpecifiers = List.of(report.createdAt.desc(), report.id.desc());
        }

        List<AnalysisReportListItem> content = queryFactory
                .select(Projections.constructor(
                        AnalysisReportListItem.class,
                        report.uploadId,
                        report.label,
                        report.score,
                        report.modelVersion,
                        report.mediaType,
                        report.inputResolution,
                        report.inferenceTimeMs,
                        report.createdAt
                ))
                .from(report)
                .where(predicate)
                .orderBy(orderSpecifiers.toArray(OrderSpecifier[]::new))
                .offset(sanitized.getOffset())
                .limit(sanitized.getPageSize())
                .fetch();

        return new PageImpl<>(content, sanitized, total);
    }

    @Override
    public Optional<AnalysisReportDetailResponse> findDetailByUserAndUploadId(Long userNo, String uploadId) {
        AnalysisReportDetailResponse detail = queryFactory
                .select(Projections.constructor(
                        AnalysisReportDetailResponse.class,
                        report.uploadId,
                        report.label,
                        report.score,
                        report.modelVersion,
                        report.mediaType,
                        report.inputResolution,
                        report.inferenceTimeMs,
                        report.createdAt,
                        report.updatedAt,
                        report.heatmapJson,
                        report.metaJson
                ))
                .from(report)
                .where(
                        report.user.userNo.eq(userNo),
                        report.uploadId.eq(uploadId)
                )
                .fetchOne();
        return Optional.ofNullable(detail);
    }

    @Override
    public List<AnalysisReportVersionStat> aggregateVersionStats(Long userNo, String modelVersion) {
        BooleanBuilder predicate = new BooleanBuilder().and(report.user.userNo.eq(userNo));
        if (modelVersion != null && !modelVersion.isBlank()) {
            predicate.and(report.modelVersion.eq(modelVersion));
        }

        NumberExpression<Long> aiCount = new CaseBuilder()
                .when(report.label.eq(AnalysisLabel.AI)).then(1L)
                .otherwise(0L)
                .sum();
        NumberExpression<Long> realCount = new CaseBuilder()
                .when(report.label.eq(AnalysisLabel.REAL)).then(1L)
                .otherwise(0L)
                .sum();
        NumberExpression<Long> unknownCount = new CaseBuilder()
                .when(report.label.eq(AnalysisLabel.UNKNOWN)).then(1L)
                .otherwise(0L)
                .sum();

        NumberExpression<Double> avgScore = report.score.avg();
        NumberExpression<Double> avgInference = report.inferenceTimeMs.avg();

        List<Tuple> rows = queryFactory
                .select(
                        report.modelVersion,
                        aiCount,
                        realCount,
                        unknownCount,
                        avgScore,
                        avgInference
                )
                .from(report)
                .where(predicate)
                .groupBy(report.modelVersion)
                .orderBy(report.modelVersion.asc())
                .fetch();

        if (CollectionUtils.isEmpty(rows)) {
            return Collections.emptyList();
        }

        return rows.stream()
                .map(tuple -> new AnalysisReportVersionStat(
                        tuple.get(report.modelVersion),
                        Optional.ofNullable(tuple.get(aiCount)).orElse(0L),
                        Optional.ofNullable(tuple.get(realCount)).orElse(0L),
                        Optional.ofNullable(tuple.get(unknownCount)).orElse(0L),
                        Optional.ofNullable(tuple.get(avgScore)).map(BigDecimal::valueOf).orElse(null),
                        Optional.ofNullable(tuple.get(avgInference)).orElse(null)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AnalysisReportSummaryStat> aggregateSummary(Long userNo, AnalysisMediaType mediaType) {
        BooleanBuilder predicate = new BooleanBuilder().and(report.user.userNo.eq(userNo));
        if (mediaType != null) {
            predicate.and(report.mediaType.eq(mediaType));
        }

        NumberExpression<Long> aiCount = new CaseBuilder()
                .when(report.label.eq(AnalysisLabel.AI)).then(1L)
                .otherwise(0L)
                .sum();
        NumberExpression<Long> realCount = new CaseBuilder()
                .when(report.label.eq(AnalysisLabel.REAL)).then(1L)
                .otherwise(0L)
                .sum();
        NumberExpression<Long> unknownCount = new CaseBuilder()
                .when(report.label.eq(AnalysisLabel.UNKNOWN)).then(1L)
                .otherwise(0L)
                .sum();

        NumberExpression<Double> avgScore = report.score.avg();
        NumberExpression<Double> avgInference = report.inferenceTimeMs.avg();
        var latestCreated = report.createdAt.max();

        Tuple tuple = queryFactory
                .select(
                        aiCount,
                        realCount,
                        unknownCount,
                        avgScore,
                        avgInference,
                        latestCreated
                )
                .from(report)
                .where(predicate)
                .fetchOne();

        if (tuple == null) {
            return Optional.empty();
        }

        return Optional.of(new AnalysisReportSummaryStat(
                Optional.ofNullable(tuple.get(aiCount)).orElse(0L),
                Optional.ofNullable(tuple.get(realCount)).orElse(0L),
                Optional.ofNullable(tuple.get(unknownCount)).orElse(0L),
                Optional.ofNullable(tuple.get(avgScore)).map(BigDecimal::valueOf).orElse(null),
                Optional.ofNullable(tuple.get(avgInference)).orElse(null),
                tuple.get(latestCreated)
        ));
    }

    private Pageable applyDefaultSort(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt")));
        }
        int pageNumber = Math.max(pageable.getPageNumber(), 0);
        int pageSize = pageable.getPageSize() <= 0 ? 10 : pageable.getPageSize();
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Order.desc("createdAt"));
        return PageRequest.of(pageNumber, pageSize, sort);
    }

    private List<OrderSpecifier<?>> resolveSort(Sort sort) {
        if (sort == null || sort.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            switch (order.getProperty()) {
                case "createdAt" -> orders.add(new OrderSpecifier<>(direction, report.createdAt));
                case "score" -> orders.add(new OrderSpecifier<>(direction, report.score));
                case "modelVersion" -> orders.add(new OrderSpecifier<>(direction, report.modelVersion));
                case "mediaType" -> orders.add(new OrderSpecifier<>(direction, report.mediaType));
                case "label" -> orders.add(new OrderSpecifier<>(direction, report.label));
                default -> {
                    // fall back to createdAt when unknown property requested
                }
            }
        }
        return orders;
    }
}
