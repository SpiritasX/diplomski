package com.example.backend.identity.internal.application;

import com.example.backend.identity.api.LoginRequest;
import com.example.backend.identity.api.LoginResponse;
import com.example.backend.identity.internal.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication =
                authenticationManager.authenticate(
                        UsernamePasswordAuthenticationToken
                                .unauthenticated(
                                        request.studentIndex(),
                                        request.password()
                                )
                );

        String accessToken = jwtService.generateAccessToken(authentication);

        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtService.expiresIn()
        );
    }
}
