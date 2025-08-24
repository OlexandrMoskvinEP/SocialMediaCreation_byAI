package com.social_media_app.controller;

import com.social_media_app.model.dto.RegisterRequest;
import com.social_media_app.model.dto.UserResponse;
import com.social_media_app.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest r) {
        var resp = authService.register(r);

        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
}

