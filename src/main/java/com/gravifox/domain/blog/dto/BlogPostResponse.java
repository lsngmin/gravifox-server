package com.gravifox.domain.blog.dto;

import com.gravifox.domain.blog.domain.BlogPost;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record BlogPostResponse(
        Long id,
        String slug,
        String title,
        String excerpt,
        String content,
        LocalDate publishedAt,
        Integer readTimeMinutes,
        List<String> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BlogPostResponse from(BlogPost post) {
        return new BlogPostResponse(
                post.getPostId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getContent(),
                post.getPublishedAt(),
                post.getReadTimeMinutes(),
                normalizeTags(post.getTags()),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    private static List<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toUnmodifiableList());
    }
}
