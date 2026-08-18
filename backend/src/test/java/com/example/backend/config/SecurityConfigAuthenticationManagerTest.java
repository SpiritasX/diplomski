package com.example.backend.config;

import com.example.backend.identity.internal.security.AccountUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigAuthenticationManagerTest {

    private final SecurityConfig securityConfig = new SecurityConfig(null);
    private final PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final AuthenticationManager authenticationManager =
            securityConfig.authenticationManager(userDetailsService, passwordEncoder);

    @Test
    void authenticatesExistingStudentWithCorrectPassword() {
        String studentIndex = "IN 20/2021";
        String rawPassword = "Test1234!";

        when(userDetailsService.loadUserByUsername(studentIndex))
                .thenReturn(new AccountUserDetails(
                        studentIndex,
                        passwordEncoder.encode(rawPassword),
                        List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
                ));

        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(studentIndex, rawPassword)
        );

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isInstanceOf(AccountUserDetails.class);
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .contains("ROLE_STUDENT");
    }

    @Test
    void failsForExistingStudentWithWrongPassword() {
        String studentIndex = "IN 20/2021";

        when(userDetailsService.loadUserByUsername(studentIndex))
                .thenReturn(new AccountUserDetails(
                        studentIndex,
                        passwordEncoder.encode("correct-password"),
                        List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
                ));

        assertThatThrownBy(() -> authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(studentIndex, "wrong-password")
        ))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void failsForMissingStudent() {
        String studentIndex = "IN 999/2026";

        when(userDetailsService.loadUserByUsername(studentIndex))
                .thenThrow(new UsernameNotFoundException("Account not found"));

        assertThatThrownBy(() -> authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(studentIndex, "password")
        ))
                .isInstanceOf(BadCredentialsException.class);
    }
}
