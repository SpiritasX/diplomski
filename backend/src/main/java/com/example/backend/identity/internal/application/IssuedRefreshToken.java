package com.example.backend.identity.internal.application;

import java.time.OffsetDateTime;

public record IssuedRefreshToken(
        String token,
        OffsetDateTime expiresAt,
        long expiresIn
) {
}
