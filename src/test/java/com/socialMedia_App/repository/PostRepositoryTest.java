package com.social_media_app.repository;

import com.social_media_app.model.Post;
import com.social_media_app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = userRepository.save(User.builder().username("alice").build());
        bob = userRepository.save(User.builder().username("bob").build());

        postRepository.save(Post.builder().title("A1").body("...").author(alice).build());
        postRepository.save(Post.builder().title("A2").body("...").author(alice).build());
        postRepository.save(Post.builder().title("B1").body("...").author(bob).build());
    }

    @Test
    void findAllByAuthorOrderByCreatedAtDesc_returnsNewestFirst() {
        Page<Post> page = postRepository.findAllByAuthorOrderByCreatedAtDesc(
                alice, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        List<Post> content = page.getContent();
        assertThat(content.get(0).getCreatedAt())
                .isAfterOrEqualTo(content.get(1).getCreatedAt());
    }

    @Test
    void findAllByAuthorIdInOrderByCreatedAtDesc_supportsFeed() {
        Page<Post> page = postRepository.findAllByAuthorIdInOrderByCreatedAtDesc(
                List.of(alice.getId(), bob.getId()), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).allMatch(p -> p.getAuthor() != null);
    }
}