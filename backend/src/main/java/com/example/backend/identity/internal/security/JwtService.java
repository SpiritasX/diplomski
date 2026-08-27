package com.example.backend.identity.internal.security;

import com.example.backend.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Collection;
import java.util.Date;

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
        return generateAccessToken(
                authentication.getName(),
                authentication.getAuthorities()
        );
    }

    public String generateAccessToken(String name, Collection<? extends GrantedAuthority> authorities) {
        var now = clock.instant();

        return Jwts.builder()
                .subject(name)
                .issuer(jwt.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(jwt.expiresIn())))
                .claim(
                        "authorities",
                        authorities
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList()
                )
                .signWith(Keys.hmacShaKeyFor(jwt.secret().getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
    }

    public long expiresIn() {
        return jwt.expiresIn();
    }
}
