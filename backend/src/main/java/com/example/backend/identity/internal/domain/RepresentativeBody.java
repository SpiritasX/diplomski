package com.example.backend.identity.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "REPRESENTATIVE_BODIES")
@Getter
public class RepresentativeBody {

    @Id
    @Column(name = "BODY_ID", unique = true, nullable = false)
    private UUID bodyId;

    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    protected RepresentativeBody() {
    }

    public RepresentativeBody(String name) {
        this.bodyId = UUID.randomUUID();
        this.name = Objects.requireNonNull(name, "name must not be null");
    }
}
