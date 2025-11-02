package com.gravifox.domain.blog.service;

import com.gravifox.domain.blog.domain.BlogPost;
import com.gravifox.domain.blog.dto.BlogPostRequest;
import com.gravifox.domain.blog.dto.BlogPostResponse;
import com.gravifox.domain.blog.repository.BlogPostRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class BlogPostService {

    private final BlogPostRepository blogPostRepository;

    public BlogPostService(BlogPostRepository blogPostRepository) {
        this.blogPostRepository = blogPostRepository;
    }

    public BlogPostResponse create(BlogPostRequest request) {
        String slug = normalizeSlug(request.slug());
        ensureSlugAvailable(slug, null);
        BlogPost post = new BlogPost(
                slug,
                normalizeText(request.title(), "title"),
                normalizeText(request.excerpt(), "excerpt"),
                normalizeContent(request.content()),
                request.publishedAt(),
                request.readTimeMinutes(),
                request.tags()
        );
        BlogPost saved = blogPostRepository.save(post);
        return BlogPostResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<BlogPostResponse> list() {
        Sort sort = Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("postId"));
        return blogPostRepository.findAll(sort)
                .stream()
                .map(BlogPostResponse::from)
                .collect(Collectors.toUnmodifiableList());
    }

    @Transactional(readOnly = true)
    public BlogPostResponse getById(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "blog_post_not_found"));
        return BlogPostResponse.from(post);
    }

    @Transactional(readOnly = true)
    public BlogPostResponse getBySlug(String slug) {
        String normalized = normalizeSlug(slug);
        BlogPost post = blogPostRepository.findBySlug(normalized)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "blog_post_not_found"));
        return BlogPostResponse.from(post);
    }

    public BlogPostResponse update(Long id, BlogPostRequest request) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "blog_post_not_found"));
        String slug = normalizeSlug(request.slug());
        ensureSlugAvailable(slug, id);
        post.update(
                slug,
                normalizeText(request.title(), "title"),
                normalizeText(request.excerpt(), "excerpt"),
                normalizeContent(request.content()),
                request.publishedAt(),
                request.readTimeMinutes(),
                request.tags()
        );
        return BlogPostResponse.from(post);
    }

    public void delete(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "blog_post_not_found"));
        blogPostRepository.delete(post);
    }

    private void ensureSlugAvailable(String slug, Long postId) {
        boolean exists;
        if (postId == null) {
            exists = blogPostRepository.existsBySlug(slug);
        } else {
            exists = blogPostRepository.existsBySlugAndPostIdNot(slug, postId);
        }
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "blog_post_slug_conflict");
        }
    }

    private String normalizeSlug(String slug) {
        Objects.requireNonNull(slug, "slug must not be null");
        String value = slug.trim();
        if (value.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "slug_blank");
        }
        return value;
    }

    private String normalizeText(String text, String fieldName) {
        Objects.requireNonNull(text, fieldName + " must not be null");
        String value = text.strip();
        if (value.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + "_blank");
        }
        return value;
    }

    private String normalizeContent(String content) {
        Objects.requireNonNull(content, "content must not be null");
        String value = content.strip();
        if (value.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content_blank");
        }
        return value;
    }
}
