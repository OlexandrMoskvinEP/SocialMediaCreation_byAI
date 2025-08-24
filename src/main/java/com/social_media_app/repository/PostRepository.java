package com.social_media_app.repository;

import com.social_media_app.model.Post;
import com.social_media_app.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);

    Page<Post> findAllByAuthorIdInOrderByCreatedAtDesc(Iterable<Long> authorIds, Pageable pageable);
}
