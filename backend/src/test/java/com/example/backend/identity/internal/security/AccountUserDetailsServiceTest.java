package com.example.backend.identity.internal.security;

import com.example.backend.identity.internal.persistence.AccountCredentialsProjection;
import com.example.backend.identity.internal.persistence.AccountRepository;
import com.example.backend.identity.internal.persistence.AdminMandateRepository;
import com.example.backend.identity.internal.persistence.RepresentativeMandateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountUserDetailsServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-08-15T12:00:00Z"),
            ZoneOffset.UTC
    );

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final AdminMandateRepository adminMandateRepository = mock(AdminMandateRepository.class);
    private final RepresentativeMandateRepository representativeMandateRepository =
            mock(RepresentativeMandateRepository.class);
    private final AccountUserDetailsService userDetailsService = new AccountUserDetailsService(
            accountRepository,
            adminMandateRepository,
            representativeMandateRepository,
            FIXED_CLOCK
    );

    @Test
    void loadsExistingStudentWithStudentRoleOnly() {
        String studentIndex = "IN 42/2022";

        when(accountRepository.findCredentialsByStudentIndex(studentIndex))
                .thenReturn(Optional.of(credentials(studentIndex)));
        when(adminMandateRepository.existsActiveMandateForStudent(studentIndex, fixedNow()))
                .thenReturn(false);

        AccountUserDetails userDetails =
                (AccountUserDetails) userDetailsService.loadUserByUsername(studentIndex);

        assertThat(userDetails.getUsername()).isEqualTo(studentIndex);
        assertThat(userDetails.getPassword()).isEqualTo("{bcrypt}hash");
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_STUDENT");
    }

    @Test
    void loadsStudentWithActiveAdminMandate() {
        String studentIndex = "IN 20/2021";

        when(accountRepository.findCredentialsByStudentIndex(studentIndex))
                .thenReturn(Optional.of(credentials(studentIndex)));
        when(adminMandateRepository.existsActiveMandateForStudent(studentIndex, fixedNow()))
                .thenReturn(true);

        AccountUserDetails userDetails =
                (AccountUserDetails) userDetailsService.loadUserByUsername(studentIndex);

        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_STUDENT", "ROLE_ADMIN");
    }

    @Test
    void loadsStudentWithoutActiveAdminMandateAsStudentOnly() {
        String studentIndex = "RA 7/2020";

        when(accountRepository.findCredentialsByStudentIndex(studentIndex))
                .thenReturn(Optional.of(credentials(studentIndex)));
        when(adminMandateRepository.existsActiveMandateForStudent(studentIndex, fixedNow()))
                .thenReturn(false);

        AccountUserDetails userDetails =
                (AccountUserDetails) userDetailsService.loadUserByUsername(studentIndex);

        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .doesNotContain("ROLE_ADMIN");
    }

    @Test
    void loadsStudentWithActiveRepresentativeMandate() {
        String studentIndex = "IN 20/2021";

        when(accountRepository.findCredentialsByStudentIndex(studentIndex))
                .thenReturn(Optional.of(credentials(studentIndex)));
        when(representativeMandateRepository.existsActiveMandateForStudent(studentIndex, fixedNow()))
                .thenReturn(true);

        AccountUserDetails userDetails =
                (AccountUserDetails) userDetailsService.loadUserByUsername(studentIndex);

        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_STUDENT", "ROLE_REPRESENTATIVE");
    }

    @Test
    void throwsWhenAccountDoesNotExist() {
        String studentIndex = "IN 999/2026";

        when(accountRepository.findCredentialsByStudentIndex(studentIndex))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(studentIndex))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Account not found");

        verify(adminMandateRepository, never())
                .existsActiveMandateForStudent(studentIndex, fixedNow());
        verify(representativeMandateRepository, never())
                .existsActiveMandateForStudent(studentIndex, fixedNow());
    }

    private static OffsetDateTime fixedNow() {
        return OffsetDateTime.now(FIXED_CLOCK);
    }

    private static AccountCredentialsProjection credentials(String studentIndex) {
        return new AccountCredentialsProjection() {
            @Override
            public String getStudentIndex() {
                return studentIndex;
            }

            @Override
            public String getPasswordHash() {
                return "{bcrypt}hash";
            }
        };
    }
}
