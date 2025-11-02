package com.gravifox.domain.blog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gravifox.domain.blog.domain.BlogPost;
import com.gravifox.domain.blog.dto.BlogPostRequest;
import com.gravifox.domain.blog.dto.BlogPostResponse;
import com.gravifox.domain.blog.repository.BlogPostRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class BlogPostServiceTest {

    @Mock
    private BlogPostRepository blogPostRepository;

    @InjectMocks
    private BlogPostService blogPostService;

    @Test
    @DisplayName("새 블로그 글을 생성하면 입력값이 정규화되어 저장된다")
    void createBlogPost() {
        BlogPostRequest request = new BlogPostRequest(
                "  new-slug  ",
                "  Title  ",
                "  Summary  ",
                "  Content body  ",
                LocalDate.of(2025, 1, 10),
                5,
                List.of(" ai ", "product", " ")
        );
        when(blogPostRepository.existsBySlug("new-slug")).thenReturn(false);
        when(blogPostRepository.save(any(BlogPost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlogPostResponse response = blogPostService.create(request);

        assertThat(response.slug()).isEqualTo("new-slug");
        assertThat(response.title()).isEqualTo("Title");
        assertThat(response.excerpt()).isEqualTo("Summary");
        assertThat(response.content()).isEqualTo("Content body");
        assertThat(response.tags()).containsExactly("ai", "product");
        verify(blogPostRepository).save(any(BlogPost.class));
    }

    @Test
    @DisplayName("중복 슬러그가 존재하면 409 예외가 발생한다")
    void createBlogPostDuplicateSlug() {
        BlogPostRequest request = new BlogPostRequest(
                "existing",
                "Title",
                "Summary",
                "Content",
                LocalDate.now(),
                3,
                List.of("ai")
        );
        when(blogPostRepository.existsBySlug("existing")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> blogPostService.create(request));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("아이디 기준으로 블로그 글을 수정하면 필드가 갱신된다")
    void updateBlogPost() {
        BlogPost existing = new BlogPost(
                "old-slug",
                "Old title",
                "Old summary",
                "Old content",
                LocalDate.of(2024, 12, 1),
                4,
                List.of("ai")
        );
        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(blogPostRepository.existsBySlugAndPostIdNot("new-slug", 1L)).thenReturn(false);

        BlogPostRequest request = new BlogPostRequest(
                " new-slug ",
                " New title ",
                " New summary ",
                " New content ",
                LocalDate.of(2025, 1, 5),
                6,
                List.of("research")
        );

        BlogPostResponse response = blogPostService.update(1L, request);

        assertThat(response.slug()).isEqualTo("new-slug");
        assertThat(existing.getTitle()).isEqualTo("New title");
        assertThat(existing.getPublishedAt()).isEqualTo(LocalDate.of(2025, 1, 5));
        assertThat(existing.getTags()).containsExactly("research");
    }

    @Test
    @DisplayName("존재하지 않는 블로그 글 삭제는 404 예외를 던진다")
    void deleteNonExistingPost() {
        when(blogPostRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> blogPostService.delete(99L));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
