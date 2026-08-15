package com.example.backend.identity.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "ENROLLMENT_STATUSES")
@Getter
public class EnrollmentStatus {

    @Id
    @Column(name = "CODE", length = 30)
    private String code;

    @Column(name = "NAME", nullable = false, length = 30)
    private String name;

    protected EnrollmentStatus() {
    }
}
