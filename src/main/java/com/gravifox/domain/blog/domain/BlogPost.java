package com.gravifox.domain.blog.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "blog_post", schema = "member",
        uniqueConstraints = @UniqueConstraint(name = "uk_blog_post_slug", columnNames = "slug"))
public class BlogPost {
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    @Id
    @Column(name = "post_id", nullable = false, updatable = false)
    private Long postId;

    @Column(name = "slug", nullable = false, length = 160, unique = true)
    private String slug;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "excerpt", nullable = false, length = 512)
    private String excerpt;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "published_at", nullable = false)
    private LocalDate publishedAt;

    @Column(name = "read_time_minutes")
    private Integer readTimeMinutes;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "blog_post_tag", schema = "member", joinColumns = @JoinColumn(name = "post_id"))
    @OrderColumn(name = "tag_order")
    @Column(name = "tag_value", nullable = false, length = 48)
    private List<String> tags = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected BlogPost() {
    }

    public BlogPost(String slug,
                    String title,
                    String excerpt,
                    String content,
                    LocalDate publishedAt,
                    Integer readTimeMinutes,
                    List<String> tags) {
        this.slug = Objects.requireNonNull(slug, "slug must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.excerpt = Objects.requireNonNull(excerpt, "excerpt must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.publishedAt = Objects.requireNonNull(publishedAt, "publishedAt must not be null");
        this.readTimeMinutes = normalizeReadTime(readTimeMinutes);
        setTags(tags);
        LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE);
        this.createdAt = now;
        this.updatedAt = now;
    }

    public BlogPost(Long postId,
                    String slug,
                    String title,
                    String excerpt,
                    String content,
                    LocalDate publishedAt,
                    Integer readTimeMinutes,
                    List<String> tags) {
        this(slug, title, excerpt, content, publishedAt, readTimeMinutes, tags);
        this.postId = Objects.requireNonNull(postId, "postId must not be null");
    }

    private Integer normalizeReadTime(Integer readTimeMinutes) {
        if (readTimeMinutes == null) {
            return null;
        }
        return Math.max(readTimeMinutes, 0);
    }

    private void setTags(List<String> newTags) {
        this.tags.clear();
        if (newTags == null) {
            return;
        }
        for (String tag : newTags) {
            if (tag == null) {
                continue;
            }
            String trimmed = tag.trim();
            if (!trimmed.isEmpty()) {
                this.tags.add(trimmed);
            }
        }
    }

    public void update(String slug,
                       String title,
                       String excerpt,
                       String content,
                       LocalDate publishedAt,
                       Integer readTimeMinutes,
                       List<String> tags) {
        this.slug = Objects.requireNonNull(slug, "slug must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.excerpt = Objects.requireNonNull(excerpt, "excerpt must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.publishedAt = Objects.requireNonNull(publishedAt, "publishedAt must not be null");
        this.readTimeMinutes = normalizeReadTime(readTimeMinutes);
        setTags(tags);
        this.updatedAt = LocalDateTime.now(DEFAULT_ZONE);
    }

    public Long getPostId() {
        return postId;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public String getContent() {
        return content;
    }

    public LocalDate getPublishedAt() {
        return publishedAt;
    }

    public Integer getReadTimeMinutes() {
        return readTimeMinutes;
    }

    public List<String> getTags() {
        return tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
