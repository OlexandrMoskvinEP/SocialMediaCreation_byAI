package com.social_media_app.controller;

import com.social_media_app.model.Post;
import com.social_media_app.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService posts;

    public PostController(PostService posts) {
        this.posts = posts;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse create(@RequestBody @Valid CreatePostRequest req) {
        Post p = posts.createPost(req.authorId(), req.title(), req.body());

        return PostResponse.from(p);
    }

    @GetMapping("/{id}")
    public PostResponse getById(@PathVariable Long id) {
        Post p = posts.getById(id);

        return PostResponse.from(p);
    }

    @GetMapping("/by-author/{authorId}")
    public PageResponse<PostResponse> listByAuthor(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Post> result = posts.listByAuthor(authorId, PageRequest.of(page, size));
        List<PostResponse> content = result.getContent().stream().map(PostResponse::from).toList();

        return PageResponse.of(content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @GetMapping("/feed/{userId}")
    public PageResponse<PostResponse> feed(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Post> result = posts.feedFor(userId, PageRequest.of(page, size));
        List<PostResponse> content = result.getContent().stream().map(PostResponse::from).toList();

        return PageResponse.of(content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    // --- DTOs ---
    public record CreatePostRequest(
            @NotNull Long authorId,
            @NotBlank @Size(max = 140) String title,
            @NotBlank @Size(max = 2000) String body
    ) {
    }

    public record PostResponse(Long id, Long authorId, String title, String body) {
        static PostResponse from(Post p) {

            return new PostResponse(p.getId(), p.getAuthor().getId(), p.getTitle(), p.getBody());
        }
    }

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
        static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements, int totalPages) {

            return new PageResponse<>(content, page, size, totalElements, totalPages);
        }
    }
}

