package com.gravifox.domain.blog.controller;

import com.gravifox.domain.blog.dto.BlogPostRequest;
import com.gravifox.domain.blog.dto.BlogPostResponse;
import com.gravifox.domain.blog.service.BlogPostService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/blog/posts")
@Validated
public class BlogPostController {

    private final BlogPostService blogPostService;

    public BlogPostController(BlogPostService blogPostService) {
        this.blogPostService = blogPostService;
    }

    @GetMapping
    public List<BlogPostResponse> list() {
        return blogPostService.list();
    }

    @GetMapping("/{id}")
    public BlogPostResponse getById(@PathVariable("id") Long id) {
        return blogPostService.getById(id);
    }

    @GetMapping("/slug/{slug}")
    public BlogPostResponse getBySlug(@PathVariable("slug") String slug) {
        return blogPostService.getBySlug(slug);
    }

    @PostMapping
    public ResponseEntity<BlogPostResponse> create(@Valid @RequestBody BlogPostRequest request) {
        BlogPostResponse response = blogPostService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public BlogPostResponse update(@PathVariable("id") Long id,
                                   @Valid @RequestBody BlogPostRequest request) {
        return blogPostService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        blogPostService.delete(id);
    }
}
