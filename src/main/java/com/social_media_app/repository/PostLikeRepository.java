package com.social_media_app.repository;

import com.social_media_app.model.Post;
import com.social_media_app.model.PostLike;
import com.social_media_app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByUserAndPost(User user, Post post);

    long countByPost(Post post);

    void deleteByUserAndPost(User user, Post post);
}
