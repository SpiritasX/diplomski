package com.example.backend.identity.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "REFRESH_TOKENS",
        uniqueConstraints = @UniqueConstraint(name = "UQ_REFRESH_TOKEN_HASH", columnNames = "TOKEN_HASH")
)
@Getter
public class RefreshToken {

    @Id
    @Column(name = "TOKEN_ID", unique = true, nullable = false)
    private UUID tokenId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SESSION_ID", nullable = false)
    private RefreshSession session;

    @Column(name = "TOKEN_HASH", nullable = false, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private RefreshTokenStatus status;

    @Column(name = "ISSUED_AT", nullable = false)
    private OffsetDateTime issuedAt;

    @Column(name = "USED_AT")
    private OffsetDateTime usedAt;

    protected RefreshToken() {
    }

    public RefreshToken(
            RefreshSession session,
            String tokenHash,
            OffsetDateTime issuedAt
    ) {
        this.session = Objects.requireNonNull(session, "session must not be null");
        this.tokenHash = Objects.requireNonNull(tokenHash, "tokenHash must not be null");
        this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt must not be null");
    }

    public boolean isActive() {
        return status == RefreshTokenStatus.ACTIVE;
    }

    public void markUsed(OffsetDateTime usedAt) {
        if (!isActive()) {
            return;
        }

        this.status = RefreshTokenStatus.USED;
        this.usedAt = Objects.requireNonNull(usedAt, "usedAt must not be null");
    }

    public void revoke() {
        if (!isActive()) {
            return;
        }

        this.status = RefreshTokenStatus.REVOKED;
    }
}
