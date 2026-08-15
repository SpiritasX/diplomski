package com.example.backend.identity.internal.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ADMIN_MANDATES")
@Getter
public class AdminMandate {

    @Id
    @Column(name = "ADMIN_MANDATE_ID", unique = true, nullable = false)
    private UUID adminMandateId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "STUDENT_INDEX", nullable = false)
    private Account account;

    @Column(
            name = "VALID_FROM",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime validFrom;

    @Column(name = "VALID_UNTIL")
    private OffsetDateTime validUntil;

    protected AdminMandate() {
    }

    public AdminMandate(Account account) {
        this.account = account;
    }

    public void endAt(OffsetDateTime validUntil) {
        this.validUntil = validUntil;
    }
}
