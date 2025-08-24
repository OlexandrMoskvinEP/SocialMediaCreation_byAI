package com.social_media_app.model.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String usernameOrEmail,
                           @NotBlank String password) {
}
