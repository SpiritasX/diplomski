package com.example.backend.identity.api;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String studentIndex,
        @NotBlank String password
) {
}
