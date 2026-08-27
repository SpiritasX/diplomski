package com.example.backend.identity.internal.application;

public record RefreshGrant(
        String studentIndex,
        IssuedRefreshToken refreshToken
) {
}
