package com.example.backend.identity.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank
        String studentIndex,

        @Schema(example = "Test1234!")
        @NotBlank
        String password
) {
}
