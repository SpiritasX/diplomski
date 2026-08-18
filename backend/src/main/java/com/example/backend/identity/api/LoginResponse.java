package com.example.backend.identity.api;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
