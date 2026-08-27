package com.example.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.refresh")
public record RefreshTokenProperties(
        long expiresIn
) {}
