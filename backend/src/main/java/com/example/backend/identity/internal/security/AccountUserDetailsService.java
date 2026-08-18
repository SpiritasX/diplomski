package com.example.backend.identity.internal.security;

import com.example.backend.identity.internal.persistence.AccountCredentialsProjection;
import com.example.backend.identity.internal.persistence.AccountRepository;
import com.example.backend.identity.internal.persistence.AdminMandateRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountUserDetailsService implements UserDetailsService {

    private static final GrantedAuthority ROLE_STUDENT = new SimpleGrantedAuthority("ROLE_STUDENT");
    private static final GrantedAuthority ROLE_ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");

    private final AccountRepository accountRepository;
    private final AdminMandateRepository adminMandateRepository;
    private final Clock clock;

    public AccountUserDetailsService(
            AccountRepository accountRepository,
            AdminMandateRepository adminMandateRepository,
            Clock clock
    ) {
        this.accountRepository = accountRepository;
        this.adminMandateRepository = adminMandateRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String studentIndex) throws UsernameNotFoundException {
        AccountCredentialsProjection credentials = accountRepository.findCredentialsByStudentIndex(studentIndex)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(ROLE_STUDENT);

        if (adminMandateRepository.existsActiveMandateForStudent(
                studentIndex,
                OffsetDateTime.now(clock)
        )) {
            authorities.add(ROLE_ADMIN);
        }

        return new AccountUserDetails(
                credentials.getStudentIndex(),
                credentials.getPasswordHash(),
                List.copyOf(authorities)
        );
    }
}
