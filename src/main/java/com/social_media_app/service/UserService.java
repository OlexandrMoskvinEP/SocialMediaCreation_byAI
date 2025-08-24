package com.social_media_app.service;

import com.social_media_app.exceptions.ConflictException;
import com.social_media_app.exceptions.NotFoundException;
import com.social_media_app.model.User;
import com.social_media_app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional
    public User createUser(String username) {
        if (users.existsByUsername(username)) {
            throw new ConflictException("Username already taken: " + username);
        }

        User user = getUser1().toBuilder()
                .username(username)
                .email(username + "@com.ua")
                .passwordHash("ojikfy")
                .build();

        return users.save(user);
    }

    public User getById(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + id));
    }

    public User getByUsername(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }

    private static User getUser1() {
        return User.builder().username("").email("")
                .active(true).createdAt(Instant.now()).passwordHash("oq3ufctv90y4t").build();
    }
}
