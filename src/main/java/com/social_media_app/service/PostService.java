package com.social_media_app.service;

import com.social_media_app.exceptions.NotFoundException;
import com.social_media_app.model.Follow;
import com.social_media_app.model.Post;
import com.social_media_app.model.User;
import com.social_media_app.repository.FollowRepository;
import com.social_media_app.repository.PostRepository;
import com.social_media_app.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository posts;
    private final UserRepository users;
    private final FollowRepository follows;

    public PostService(PostRepository posts, UserRepository users, FollowRepository follows) {
        this.posts = posts;
        this.users = users;
        this.follows = follows;
    }

    @Transactional
    public Post createPost(Long authorId, String title, String body) {
        User author = users.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Author not found: id=" + authorId));
        Post post = Post.builder()
                .author(author)
                .title(title)
                .body(body)
                .build();
        return posts.save(post);
    }

    public Post getById(Long id) {
        return posts.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found: id=" + id));
    }

    public Page<Post> listByAuthor(Long authorId, Pageable pageable) {
        User author = users.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Author not found: id=" + authorId));
        return posts.findAllByAuthorOrderByCreatedAtDesc(author, pageable);
    }

    /**
     * Feed = posts of people I follow, newest first
     */
    public Page<Post> feedFor(Long userId, Pageable pageable) {
        User me = users.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
        List<Follow> following = follows.findAllByFollower(me);
        List<Long> authorIds = following.stream()
                .map(f -> f.getFollowed().getId())
                .toList();
        if (authorIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return posts.findAllByAuthorIdInOrderByCreatedAtDesc(authorIds, pageable);
    }
}
