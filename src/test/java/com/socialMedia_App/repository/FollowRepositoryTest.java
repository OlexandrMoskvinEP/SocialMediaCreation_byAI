package com.social_media_app.repository;

import com.social_media_app.model.Follow;
import com.social_media_app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FollowRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FollowRepository followRepository;

    private User alice;
    private User bob;
    private User carol;

    @BeforeEach
    void setUp() {
        alice = userRepository.save(User.builder().username("alice").build());
        bob = userRepository.save(User.builder().username("bob").build());
        carol = userRepository.save(User.builder().username("carol").build());

        followRepository.save(Follow.builder().follower(alice).followed(bob).build());
        followRepository.save(Follow.builder().follower(alice).followed(carol).build());
        followRepository.save(Follow.builder().follower(bob).followed(alice).build());
    }

    @Test
    void existsAndFindPair() {
        assertThat(followRepository.existsByFollowerAndFollowed(alice, bob)).isTrue();
        assertThat(followRepository.findByFollowerAndFollowed(alice, bob)).isPresent();
        assertThat(followRepository.existsByFollowerAndFollowed(bob, carol)).isFalse();
    }

    @Test
    void listFollowersAndFollowing() {
        List<Follow> followingOfAlice = followRepository.findAllByFollower(alice);
        List<Follow> followersOfAlice = followRepository.findAllByFollowed(alice);

        assertThat(followingOfAlice).hasSize(2);
        assertThat(followersOfAlice).hasSize(1);
        assertThat(followersOfAlice.get(0).getFollower().getUsername()).isEqualTo("bob");
    }
}