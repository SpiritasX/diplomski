package com.example.backend.identity.internal.application;

import com.example.backend.identity.api.LoginRequest;
import com.example.backend.identity.api.LoginResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;

    public AuthService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
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

        return null;
    }
}
