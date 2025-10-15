package com.gravifox.domain.analysisreport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

@Schema(description = "표준 페이지 응답")
public record PageResponse<T>(
        @Schema(description = "목록 데이터") List<T> items,
        @Schema(description = "페이지 번호(0부터 시작)", example = "0") int page,
        @Schema(description = "페이지 크기", example = "10") int size,
        @Schema(description = "전체 요소 수", example = "128") long totalElements,
        @Schema(description = "전체 페이지 수", example = "13") int totalPages,
        @Schema(description = "정렬 정보") List<SortOrder> sort
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        List<SortOrder> sortOrders = page.getSort().stream()
                .map(order -> new SortOrder(order.getProperty(), order.isAscending() ? SortDirection.ASC : SortDirection.DESC))
                .toList();

        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                sortOrders
        );
    }

    @Schema(description = "정렬 항목")
    public record SortOrder(
            @Schema(description = "정렬 필드", example = "createdAt") String property,
            @Schema(description = "정렬 방향", example = "DESC") SortDirection direction
    ) {
    }

    public enum SortDirection {
        ASC, DESC
    }
}
