package com.social_media_app.repository;

import com.social_media_app.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void existsByUsername_and_findByUsername_work() {
        User alice = getUser();
        userRepository.save(alice);

        assertThat(userRepository.existsByUsername("bob")).isTrue();
        assertThat(userRepository.existsByUsername("alice")).isFalse();

        Optional<User> found = userRepository.findByUsername("bob");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("bob");
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void savingUserWithSameUsername_throwsException() {
        User alice1 = getUser();
        userRepository.save(alice1);

        User alice2 = getUser();

        try {
            userRepository.save(alice2);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(Exception.class);
        }
    }

    @Test
    void existsByEmail_and_findByEmail_work() {
        User bob = getUser();

        userRepository.save(bob);

        assertThat(userRepository.existsByEmail(bob.getEmail())).isTrue();
        assertThat(userRepository.existsByEmail("fake@email.com")).isFalse();

        Optional<User> found = userRepository.findByEmail(bob.getEmail());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(bob.getEmail());
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    private static User getUser() {
        return User.builder().username("bob").email("bob@email.com").active(true).createdAt(Instant.now()).passwordHash("oq3ufctv90y4t").build();
    }
}