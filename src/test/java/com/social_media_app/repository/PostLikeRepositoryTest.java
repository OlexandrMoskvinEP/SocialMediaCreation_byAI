package com.social_media_app.repository;

import com.social_media_app.model.Post;
import com.social_media_app.model.PostLike;
import com.social_media_app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostLikeRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PostLikeRepository postLikeRepository;

    private User alice;
    private User bob;
    private Post post;

    @BeforeEach
    void setUp() {
        alice = userRepository.save(getUser2());
        bob = userRepository.save(getUser1());
        post = postRepository.save(Post.builder().title("Hello").body("Body").author(alice).build());
    }

    @Test
    void likeExistsCountAndDelete() {
        postLikeRepository.save(PostLike.builder().user(bob).post(post).build());

        assertThat(postLikeRepository.existsByUserAndPost(bob, post)).isTrue();
        assertThat(postLikeRepository.countByPost(post)).isEqualTo(1L);

        postLikeRepository.deleteByUserAndPost(bob, post);
        assertThat(postLikeRepository.existsByUserAndPost(bob, post)).isFalse();
        assertThat(postLikeRepository.countByPost(post)).isZero();
    }

    private static User getUser1() {
        return User.builder().username("bob").email("bob@email.com").active(true).createdAt(Instant.now()).passwordHash("oq3ufctv90y4t").build();
    }

    private static User getUser2() {
        return User.builder().username("alice").email("alice@email.com").active(true).createdAt(Instant.now()).passwordHash("oq3ufctv90y4t").build();
    }
}