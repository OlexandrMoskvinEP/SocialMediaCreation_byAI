package com.social_media_app.controller;

import com.social_media_app.model.User;
import com.social_media_app.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestBody @Valid CreateUserRequest req) {
        User u = users.createUser(req.username());

        return UserResponse.from(u);
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        User u = users.getById(id);

        return UserResponse.from(u);
    }

    @GetMapping("/by-username/{username}")
    public UserResponse getByUsername(@PathVariable String username) {
        User u = users.getByUsername(username);

        return UserResponse.from(u);
    }

    // --- DTOs (compact + local to controller for simplicity) ---
    public record CreateUserRequest(@NotBlank String username) {
    }

    public record UserResponse(Long id, String username) {
        static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getUsername());
        }
    }
}

