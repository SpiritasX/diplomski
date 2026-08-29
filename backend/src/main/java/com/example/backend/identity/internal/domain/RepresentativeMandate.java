package com.example.backend.identity.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "REPRESENTATIVE_MANDATES")
@Getter
public class RepresentativeMandate {

    @Id
    @Column(name = "MANDATE_ID", unique = true, nullable = false)
    private UUID mandateId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "STUDENT_INDEX", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BODY_ID", nullable = false)
    private RepresentativeBody body;

    @Column(
            name = "VALID_FROM",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime validFrom;

    @Column(name = "VALID_UNTIL")
    private OffsetDateTime validUntil;

    protected RepresentativeMandate() {
    }

    public RepresentativeMandate(Account account, RepresentativeBody body) {
        this.mandateId = UUID.randomUUID();
        this.account = Objects.requireNonNull(account, "account must not be null");
        this.body = Objects.requireNonNull(body, "body must not be null");
    }

    public void endAt(OffsetDateTime validUntil) {
        this.validUntil = validUntil;
    }
}
