package com.social_media_app.service;

import com.social_media_app.model.User;
import com.social_media_app.model.dto.AuthResponse;
import com.social_media_app.model.dto.LoginRequest;
import com.social_media_app.model.dto.RegisterRequest;
import com.social_media_app.model.dto.UserResponse;
import com.social_media_app.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
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

    public AuthResponse login(LoginRequest r) {
        String login = r.usernameOrEmail().trim();

        var user = users.findByUsername(login)
                .or(() -> users.findByEmail(login.toLowerCase()))
                .orElseThrow(() -> new BadCredentialsException("bad credentials"));

        if (!encoder.matches(r.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("bad credentials");
        }

        return new AuthResponse(jwt.generate(user.getUsername()));
    }
}


