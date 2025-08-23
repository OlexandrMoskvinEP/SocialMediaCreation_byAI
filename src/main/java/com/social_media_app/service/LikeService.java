package com.social_media_app.service;

import com.social_media_app.exceptions.ConflictException;
import com.social_media_app.exceptions.NotFoundException;
import com.social_media_app.model.Post;
import com.social_media_app.model.PostLike;
import com.social_media_app.model.User;
import com.social_media_app.repository.PostLikeRepository;
import com.social_media_app.repository.PostRepository;
import com.social_media_app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LikeService {

    public static final String USER_NOT_FOUND_ID = "User not found: id=";
    public static final String POST_NOT_FOUND_ID = "Post not found: id=";
    private final PostLikeRepository likes;
    private final UserRepository users;
    private final PostRepository posts;

    public LikeService(PostLikeRepository likes, UserRepository users, PostRepository posts) {
        this.likes = likes;
        this.users = users;
        this.posts = posts;
    }

    @Transactional
    public void like(Long userId, Long postId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_ID + userId));
        Post post = posts.findById(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND_ID + postId));

        if (likes.existsByUserAndPost(user, post)) {
            throw new ConflictException("Already liked");
        }
        likes.save(PostLike.builder().user(user).post(post).build());
    }

    @Transactional
    public void unlike(Long userId, Long postId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_ID + userId));
        Post post = posts.findById(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND_ID + postId));

        likes.deleteByUserAndPost(user, post);
    }

    public long countLikes(Long postId) {
        Post post = posts.findById(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND_ID + postId));
        return likes.countByPost(post);
    }

    public boolean isLiked(Long userId, Long postId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_ID + userId));
        Post post = posts.findById(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND_ID + postId));
        return likes.existsByUserAndPost(user, post);
    }
}
