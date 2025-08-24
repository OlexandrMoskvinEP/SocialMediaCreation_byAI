package com.social_media_app.service;

import com.social_media_app.exceptions.ConflictException;
import com.social_media_app.exceptions.NotFoundException;
import com.social_media_app.model.User;
import com.social_media_app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(UserService.class)
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @Test
    void createUser_success_whenUsernameIsFree() {
        User created = userService.createUser("alice");

        assertThat(created.getId()).isNotNull();
        assertThat(created.getUsername()).isEqualTo("alice");
        assertThat(created.getCreatedAt()).isNotNull();
    }

    @Test
    void createUser_throwsConflict_whenUsernameTaken() {
        userRepository.save(getUser1());

        assertThatThrownBy(() -> userService.createUser("bob"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Username already taken");
    }

    @Test
    void getById_returnsUser_whenExists() {
        User u = userRepository.save(getUser3());

        User found = userService.getById(u.getId());
        assertThat(found.getUsername()).isEqualTo("carol");
    }

    @Test
    void getById_throwsNotFound_whenMissing() {
        assertThatThrownBy(() -> userService.getById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getByUsername_returnsUser_whenExists() {
        userRepository.save(getUser2());

        User found = userService.getByUsername("alice");
        assertThat(found.getId()).isNotNull();
        assertThat(found.getUsername()).isEqualTo("alice");
    }

    @Test
    void getByUsername_throwsNotFound_whenMissing() {
        assertThatThrownBy(() -> userService.getByUsername("nobody"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }
    private static User getUser1() {
        return User.builder().username("bob").email("bob@email.com").active(true).createdAt(Instant.now()).passwordHash("oq3ufctv90y4t").build();
    }

    private static User getUser2() {
        return User.builder().username("alice").email("alice@email.com").active(true).createdAt(Instant.now()).passwordHash("oq3ufctv90y4t").build();
    }

    private static User getUser3() {
        return User.builder().username("carol").email("carol@email.com").active(true).createdAt(Instant.now()).passwordHash("oq3ufctv90y4t").build();
    }
}
