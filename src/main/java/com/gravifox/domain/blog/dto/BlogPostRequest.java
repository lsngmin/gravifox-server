package com.gravifox.domain.blog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record BlogPostRequest(
        @NotBlank @Size(max = 160) String slug,
        @NotBlank @Size(max = 200) String title,
        @NotBlank String excerpt,
        @NotBlank String content,
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate publishedAt,
        @PositiveOrZero Integer readTimeMinutes,
        @Size(max = 16) List<@Size(max = 48) String> tags
) {
}
