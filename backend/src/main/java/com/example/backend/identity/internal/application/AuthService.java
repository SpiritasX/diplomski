package com.example.backend.identity.internal.application;

import com.example.backend.identity.api.LoginRequest;
import com.example.backend.identity.api.RefreshRequest;
import com.example.backend.identity.api.TokenResponse;
import com.example.backend.identity.internal.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            UserDetailsService userDetailsService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userDetailsService = userDetailsService;
    }

    public TokenResponse login(LoginRequest request) {
        Authentication authentication =
                authenticationManager.authenticate(
                        UsernamePasswordAuthenticationToken
                                .unauthenticated(
                                        request.studentIndex(),
                                        request.password()
                                )
                );

        String accessToken = jwtService.generateAccessToken(authentication);
        IssuedRefreshToken refreshToken =
                refreshTokenService.createSession(authentication.getName());

        return new TokenResponse(
                accessToken,
                "Bearer",
                jwtService.expiresIn(),
                refreshToken.token(),
                refreshToken.expiresIn()
        );
    }

    public TokenResponse refresh(RefreshRequest request) {
        RefreshGrant grant = refreshTokenService.rotate(request.refreshToken());
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(grant.studentIndex());
        String accessToken = jwtService.generateAccessToken(
                userDetails.getUsername(),
                userDetails.getAuthorities()
        );

        return new TokenResponse(
                accessToken,
                "Bearer",
                jwtService.expiresIn(),
                grant.refreshToken().token(),
                grant.refreshToken().expiresIn()
        );
    }

    public void logout(RefreshRequest request) {
        refreshTokenService.logout(request.refreshToken());
    }
}
