package com.social_media_app.controller;

import com.social_media_app.service.LikeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likes;

    public LikeController(LikeService likes) {
        this.likes = likes;
    }

    /** Like a post (idempotency is handled at service level via ConflictException) */
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void like(@RequestBody @Valid LikeRequest req) {
        likes.like(req.userId(), req.postId());
    }

    /** Unlike a post (no error if relation is absent — operation is effectively idempotent) */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(@RequestBody @Valid LikeRequest req) {
        likes.unlike(req.userId(), req.postId());
    }

    /** Total likes for a post */
    @GetMapping("/count/{postId}")
    public CountResponse count(@PathVariable Long postId) {
        long count = likes.countLikes(postId);

        return new CountResponse(postId, count);
    }

    /** Check if a user liked a post */
    @GetMapping("/check")
    public LikedResponse isLiked(@RequestParam Long userId, @RequestParam Long postId) {
        boolean liked = likes.isLiked(userId, postId);

        return new LikedResponse(userId, postId, liked);
    }

    // --- DTOs ---
    public record LikeRequest(@NotNull Long userId, @NotNull Long postId) {}
    public record CountResponse(Long postId, long count) {}
    public record LikedResponse(Long userId, Long postId, boolean liked) {}
}

