package com.social_media_app.controller;

import com.social_media_app.model.Follow;
import com.social_media_app.model.User;
import com.social_media_app.service.FollowService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    private final FollowService follows;

    public FollowController(FollowService follows) {
        this.follows = follows;
    }

    /**
     * Create a follow relation
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FollowResponse follow(@RequestBody @Valid FollowRequest req) {
        Follow f = follows.follow(req.followerId(), req.followedId());

        return FollowResponse.from(f);
    }

    /**
     * Remove a follow relation (idempotent)
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(@RequestBody @Valid FollowRequest req) {
        follows.unfollow(req.followerId(), req.followedId());
    }

    /**
     * Who {userId} follows
     */
    @GetMapping("/following/{userId}")
    public List<FollowResponse> listFollowing(@PathVariable Long userId) {
        return follows.listFollowing(userId).stream().map(FollowResponse::from).toList();
    }

    /**
     * Who follows {userId}
     */
    @GetMapping("/followers/{userId}")
    public List<FollowResponse> listFollowers(@PathVariable Long userId) {
        return follows.listFollowers(userId).stream().map(FollowResponse::from).toList();
    }

    // --- DTOs ---
    public record FollowRequest(@NotNull Long followerId, @NotNull Long followedId) {
    }

    public record FollowResponse(Long id, Long followerId, Long followedId) {
        static FollowResponse from(Follow f) {
            User follower = f.getFollower();
            User followed = f.getFollowed();
            return new FollowResponse(
                    f.getId(),
                    follower != null ? follower.getId() : null,
                    followed != null ? followed.getId() : null
            );
        }
    }
}

