package com.gravifox.domain.blog.dto;

import com.gravifox.domain.blog.domain.BlogPost;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
                List.copyOf(post.getTags()),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
