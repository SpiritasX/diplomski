package com.example.backend.identity.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "STUDY_PROGRAMS")
@Getter
public class StudyProgram {

    @Id
    @Column(name = "CODE", length = 3)
    private String code;

    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    protected StudyProgram() {
    }
}
