package com.social_media_app.service;

import com.social_media_app.model.User;
import com.social_media_app.model.dto.RegisterRequest;
import com.social_media_app.model.dto.UserResponse;
import com.social_media_app.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest r) {
        String username = r.username().trim();
        String email = r.email().trim().toLowerCase();

        if (users.existsByUsername(username)) throw new IllegalArgumentException("username taken");
        if (users.existsByEmail(email))      throw new IllegalArgumentException("email taken");

        var u = User.builder()
                .username(username)
                .email(email)
                .passwordHash(encoder.encode(r.password()))
                .createdAt(Instant.now())
                .active(true)
                .build();

        var saved = users.save(u);

        return new UserResponse(saved.getId(), saved.getUsername(), saved.getEmail());
    }
}


