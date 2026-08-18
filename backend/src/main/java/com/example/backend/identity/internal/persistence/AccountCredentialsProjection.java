package com.example.backend.identity.internal.persistence;

public interface AccountCredentialsProjection {

    String getStudentIndex();

    String getPasswordHash();
}
