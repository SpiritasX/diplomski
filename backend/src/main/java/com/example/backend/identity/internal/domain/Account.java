package com.example.backend.identity.internal.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ACCOUNTS")
@Getter
public class Account {

    @Id
    @Column(name = "STUDENT_INDEX", unique = true, nullable = false, length = 20)
    @Pattern(regexp = "^[A-Z]{2,3} \\d{1,3}/\\d{4}$")
    private String studentIndex;

    @Column(name = "FIRST_NAME", nullable = false, length = 100)
    private String firstName;

    @Column(name = "LAST_NAME", nullable = false, length = 100)
    private String lastName;

    @Column(name = "EMAIL", nullable = false, length = 255)
    private String email;

    @Column(name = "PASSWORD_HASH", nullable = false, length = 255)
    @Getter(AccessLevel.NONE)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "STUDY_PROGRAM_CODE", nullable = false)
    private StudyProgram studyProgram;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENROLLMENT_STATUS_CODE", nullable = false)
    private EnrollmentStatus enrollmentStatus;

    @Column(
            name = "CREATED_AT",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    protected Account() {
    }

    public Account(
            String studentIndex,
            String firstName,
            String lastName,
            String email,
            String passwordHash,
            StudyProgram studyProgram,
            EnrollmentStatus enrollmentStatus
    ) {
        this.studentIndex = studentIndex;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.studyProgram = studyProgram;
        this.enrollmentStatus = enrollmentStatus;
    }

    public void changeEmail(String email) {
        this.email = email;
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void changeEnrollmentStatus(EnrollmentStatus enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }
}