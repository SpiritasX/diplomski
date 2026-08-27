package com.example.backend.identity.internal.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "REFRESH_SESSIONS")
@Getter
public class RefreshSession {

    @Id
    @Column(name = "SESSION_ID", unique = true, nullable = false)
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "STUDENT_INDEX", nullable = false)
    private Account account;

    @Column(name = "CREATED_AT", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "EXPIRES_AT", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "LAST_USED_AT")
    private OffsetDateTime lastUsedAt;

    @Column(name = "REVOKED_AT")
    private OffsetDateTime revokedAt;

    @Column(name = "REVOKE_REASON", length = 100)
    private String revokeReason;

    protected RefreshSession() {
    }

    public RefreshSession(
            Account account,
            OffsetDateTime createdAt,
            OffsetDateTime expiresAt
    ) {
        this.sessionId = UUID.randomUUID();
        this.account = Objects.requireNonNull(account, "account must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    public boolean isExpired(OffsetDateTime now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public void markUsed(OffsetDateTime usedAt) {
        this.lastUsedAt = Objects.requireNonNull(usedAt, "usedAt must not be null");
    }

    public void revoke(OffsetDateTime revokedAt, String revokeReason) {
        if (isRevoked()) {
            return;
        }

        this.revokedAt = Objects.requireNonNull(revokedAt, "revokedAt must not be null");
        this.revokeReason = Objects.requireNonNull(revokeReason, "revokeReason must not be null");
    }
}
