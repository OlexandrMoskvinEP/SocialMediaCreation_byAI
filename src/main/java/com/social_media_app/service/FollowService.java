package com.social_media_app.service;

import com.social_media_app.exceptions.ConflictException;
import com.social_media_app.exceptions.NotFoundException;
import com.social_media_app.model.Follow;
import com.social_media_app.model.User;
import com.social_media_app.repository.FollowRepository;
import com.social_media_app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository follows;
    private final UserRepository users;

    public FollowService(FollowRepository follows, UserRepository users) {
        this.follows = follows;
        this.users = users;
    }

    @Transactional
    public Follow follow(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            throw new ConflictException("You cannot follow yourself");
        }
        User follower = users.findById(followerId)
                .orElseThrow(() -> new NotFoundException("Follower user not found: id=" + followerId));
        User followed = users.findById(followedId)
                .orElseThrow(() -> new NotFoundException("Followed user not found: id=" + followedId));

        if (follows.existsByFollowerAndFollowed(follower, followed)) {
            throw new ConflictException("Already following: " + followerId + " -> " + followedId);
        }
        return follows.save(Follow.builder().follower(follower).followed(followed).build());
    }

    @Transactional
    public void unfollow(Long followerId, Long followedId) {
        User follower = users.findById(followerId)
                .orElseThrow(() -> new NotFoundException("Follower user not found: id=" + followerId));
        User followed = users.findById(followedId)
                .orElseThrow(() -> new NotFoundException("Followed user not found: id=" + followedId));

        follows.findByFollowerAndFollowed(follower, followed)
                .ifPresent(follows::delete);
    }

    public List<Follow> listFollowing(Long userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
        return follows.findAllByFollower(user);
    }

    public List<Follow> listFollowers(Long userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
        return follows.findAllByFollowed(user);
    }
}
