package com.example.backend.identity.internal.security;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class AccountUserDetails implements UserDetails, CredentialsContainer {

    private final String studentIndex;
    private String passwordHash;
    private final Collection<? extends GrantedAuthority> authorities;

    public AccountUserDetails(String studentIndex, String passwordHash, Collection<? extends GrantedAuthority> authorities) {
        this.studentIndex = Objects.requireNonNull(studentIndex, "studentIndex must not be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        this.authorities = List.copyOf(Objects.requireNonNull(authorities, "authorities must not be null"));
    }

    @Override
    public String getUsername() {
        return studentIndex;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public void eraseCredentials() {
        this.passwordHash = null;
    }
}
