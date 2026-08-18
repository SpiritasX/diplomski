package com.example.backend.identity.internal.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final Clock clock;
    private final SecretKey secretKey;
    private final long expiresIn;

    public JwtService(
            Clock clock,
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expires-in}") long expiresIn
    ) {
        this.clock = clock;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiresIn = expiresIn;
    }

    public String generateAccessToken(Authentication authentication) {
        var now = clock.instant();

        return Jwts.builder()
                .subject(authentication.getName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiresIn)))
                .claim("authorities", authorities(authentication))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public long expiresIn() {
        return expiresIn;
    }

    private List<String> authorities(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }
}