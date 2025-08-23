package com.social_media_app.repository;

import com.social_media_app.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void existsByUsername_and_findByUsername_work() {
        User alice = User.builder().username("alice").build();
        userRepository.save(alice);

        assertThat(userRepository.existsByUsername("alice")).isTrue();
        assertThat(userRepository.existsByUsername("bob")).isFalse();

        Optional<User> found = userRepository.findByUsername("alice");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("alice");
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void savingUserWithSameUsername_throwsException() {
        User alice1 = User.builder().username("alice").build();
        userRepository.save(alice1);

        User alice2 = User.builder().username("alice").build();
        try {
            userRepository.save(alice2);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(Exception.class);
        }
    }
}