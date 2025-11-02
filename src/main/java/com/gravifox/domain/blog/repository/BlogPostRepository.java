package com.gravifox.domain.blog.repository;

import com.gravifox.domain.blog.domain.BlogPost;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    Optional<BlogPost> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndPostIdNot(String slug, Long postId);
}
