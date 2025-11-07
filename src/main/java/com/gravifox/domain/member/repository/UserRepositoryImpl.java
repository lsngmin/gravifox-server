package com.gravifox.domain.member.repository;

import com.gravifox.domain.admin.dto.AdminUserSummaryQueryResult;
import com.gravifox.domain.admin.repository.AdminUserRepositoryCustom;
import com.gravifox.domain.analysis.domain.QAnalyzeMonthlyQuota;
import com.gravifox.domain.analysisreport.domain.QAnalysisReport;
import com.gravifox.domain.member.domain.QProfile;
import com.gravifox.domain.member.domain.user.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class UserRepositoryImpl implements AdminUserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public UserRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<AdminUserSummaryQueryResult> findAdminUserSummaries(String keyword, YearMonth targetMonth, Pageable pageable) {
        QUser user = QUser.user;
        QProfile profile = QProfile.profile;
        QAnalyzeMonthlyQuota quota = QAnalyzeMonthlyQuota.analyzeMonthlyQuota;
        QAnalysisReport analysisReport = QAnalysisReport.analysisReport;

        BooleanBuilder predicate = new BooleanBuilder();
        if (StringUtils.hasText(keyword)) {
            String likeKeyword = "%" + keyword.toLowerCase() + "%";
            predicate.and(
                    user.userId.lower().like(likeKeyword)
                            .or(profile.nickname.lower().like(likeKeyword))
            );
        }

        JPAQuery<AdminUserSummaryQueryResult> contentQuery = queryFactory
                .select(Projections.constructor(
                        AdminUserSummaryQueryResult.class,
                        user.userNo,
                        user.userId,
                        profile.nickname,
                        user.loginType,
                        user.emailVerified,
                        profile.createdAt,
                        JPAExpressions
                                .select(analysisReport.createdAt.max())
                                .from(analysisReport)
                                .where(analysisReport.user.userNo.eq(user.userNo)),
                        quota.limit,
                        quota.usedCount
                ))
                .from(user)
                .leftJoin(profile).on(profile.user.eq(user))
                .leftJoin(quota).on(
                        quota.userNo.eq(user.userNo)
                                .and(quota.year.eq(targetMonth.getYear()))
                                .and(quota.month.eq(targetMonth.getMonthValue()))
                )
                .where(predicate);

        List<OrderSpecifier<?>> orderSpecifiers = resolveSort(pageable.getSort(), user, profile, quota);
        if (orderSpecifiers.isEmpty()) {
            orderSpecifiers = List.of(user.userNo.desc());
        }
        contentQuery.orderBy(orderSpecifiers.toArray(OrderSpecifier[]::new));
        contentQuery.offset(pageable.getOffset());
        contentQuery.limit(pageable.getPageSize());

        List<AdminUserSummaryQueryResult> content = contentQuery.fetch();

        Long total = queryFactory
                .select(user.count())
                .from(user)
                .leftJoin(profile).on(profile.user.eq(user))
                .where(predicate)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private List<OrderSpecifier<?>> resolveSort(Sort sort, QUser user, QProfile profile, QAnalyzeMonthlyQuota quota) {
        if (sort == null || sort.isUnsorted()) {
            return List.of();
        }
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            switch (order.getProperty()) {
                case "userNo" -> orderSpecifiers.add(new OrderSpecifier<>(direction, user.userNo));
                case "userId" -> orderSpecifiers.add(new OrderSpecifier<>(direction, user.userId));
                case "nickname" -> orderSpecifiers.add(new OrderSpecifier<>(direction, profile.nickname));
                case "monthlyQuotaUsed" -> orderSpecifiers.add(new OrderSpecifier<>(direction, quota.usedCount));
                default -> {
                }
            }
        }
        return orderSpecifiers;
    }
}
