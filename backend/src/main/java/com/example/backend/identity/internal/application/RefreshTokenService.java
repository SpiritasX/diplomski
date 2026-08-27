package com.example.backend.identity.internal.application;

import com.example.backend.config.RefreshTokenGenerator;
import com.example.backend.config.RefreshTokenHasher;
import com.example.backend.config.RefreshTokenProperties;
import com.example.backend.identity.internal.domain.Account;
import com.example.backend.identity.internal.domain.RefreshSession;
import com.example.backend.identity.internal.domain.RefreshToken;
import com.example.backend.identity.internal.domain.RefreshTokenStatus;
import com.example.backend.identity.internal.persistence.AccountRepository;
import com.example.backend.identity.internal.persistence.RefreshSessionRepository;
import com.example.backend.identity.internal.persistence.RefreshTokenRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;

@Service
public class RefreshTokenService {

    private static final String REVOKED_BY_LOGOUT = "LOGOUT";
    private static final String REVOKED_BY_ROTATION_REUSE = "ROTATION_REUSE";
    private static final String REVOKED_BY_EXPIRATION = "EXPIRED";

    private final AccountRepository accountRepository;
    private final RefreshSessionRepository refreshSessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenHasher refreshTokenHasher;
    private final RefreshTokenProperties refreshTokenProperties;
    private final Clock clock;

    public RefreshTokenService(
            AccountRepository accountRepository,
            RefreshSessionRepository refreshSessionRepository,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenGenerator refreshTokenGenerator,
            RefreshTokenHasher refreshTokenHasher,
            RefreshTokenProperties refreshTokenProperties,
            Clock clock
    ) {
        this.accountRepository = accountRepository;
        this.refreshSessionRepository = refreshSessionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenHasher = refreshTokenHasher;
        this.refreshTokenProperties = refreshTokenProperties;
        this.clock = clock;
    }

    @Transactional
    public IssuedRefreshToken createSession(String studentIndex) {
        OffsetDateTime now = now();
        OffsetDateTime expiresAt = now.plusSeconds(refreshTokenProperties.expiresIn());
        Account account = accountRepository.getReferenceById(studentIndex);

        RefreshSession session = refreshSessionRepository.save(
                new RefreshSession(account, now, expiresAt)
        );

        return issueToken(session, now);
    }

    @Transactional(noRollbackFor = BadCredentialsException.class)
    public RefreshGrant rotate(String rawToken) {
        String tokenHash = refreshTokenHasher.hash(rawToken);
        RefreshToken currentToken = refreshTokenRepository.findForUpdateByTokenHash(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        OffsetDateTime now = now();
        RefreshSession session = currentToken.getSession();

        if (!currentToken.isActive()) {
            revokeSession(session, now, REVOKED_BY_ROTATION_REUSE);
            throw new BadCredentialsException("Invalid refresh token");
        }

        if (session.isRevoked()) {
            currentToken.revoke();
            throw new BadCredentialsException("Invalid refresh token");
        }

        if (session.isExpired(now)) {
            currentToken.revoke();
            revokeSession(session, now, REVOKED_BY_EXPIRATION);
            throw new BadCredentialsException("Refresh token expired");
        }

        currentToken.markUsed(now);
        session.markUsed(now);

        IssuedRefreshToken nextToken = issueToken(session, now);

        return new RefreshGrant(
                session.getAccount().getStudentIndex(),
                nextToken
        );
    }

    @Transactional
    public void logout(String rawToken) {
        String tokenHash = refreshTokenHasher.hash(rawToken);
        RefreshToken token = refreshTokenRepository.findForUpdateByTokenHash(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        revokeSession(token.getSession(), now(), REVOKED_BY_LOGOUT);
    }

    private IssuedRefreshToken issueToken(
            RefreshSession session,
            OffsetDateTime issuedAt
    ) {
        String rawToken = refreshTokenGenerator.generate();
        RefreshToken token = new RefreshToken(
                session,
                refreshTokenHasher.hash(rawToken),
                issuedAt
        );

        refreshTokenRepository.save(token);

        return new IssuedRefreshToken(
                rawToken,
                session.getExpiresAt(),
                secondsUntil(session.getExpiresAt(), issuedAt)
        );
    }

    private void revokeSession(
            RefreshSession session,
            OffsetDateTime revokedAt,
            String reason
    ) {
        session.revoke(revokedAt, reason);

        refreshTokenRepository.findBySessionAndStatusForUpdate(session, RefreshTokenStatus.ACTIVE)
                .forEach(RefreshToken::revoke);
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(clock);
    }

    private static long secondsUntil(
            OffsetDateTime expiresAt,
            OffsetDateTime now
    ) {
        return Math.max(0, Duration.between(now, expiresAt).toSeconds());
    }
}
