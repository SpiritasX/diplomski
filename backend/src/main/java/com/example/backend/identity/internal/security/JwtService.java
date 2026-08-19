package com.example.backend.identity.internal.security;

import com.example.backend.config.JwtProperties;
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
    private final JwtProperties jwt;

    public JwtService(
            Clock clock,
            JwtProperties jwt
    ) {
        this.clock = clock;
        this.jwt = jwt;
    }

    public String generateAccessToken(Authentication authentication) {
        var now = clock.instant();

        return Jwts.builder()
                .subject(authentication.getName())
                .issuer(jwt.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(jwt.expiresIn())))
                .claim("authorities", authorities(authentication))
                .signWith(Keys.hmacShaKeyFor(jwt.secret().getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
    }

    public long expiresIn() {
        return jwt.expiresIn();
    }

    private List<String> authorities(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }
}