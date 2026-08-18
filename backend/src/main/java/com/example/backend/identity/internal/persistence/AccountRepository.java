package com.example.backend.identity.internal.persistence;

import com.example.backend.identity.internal.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    @Query("""
            SELECT
                a.studentIndex AS studentIndex,
                a.passwordHash AS passwordHash
            FROM Account a
            WHERE a.studentIndex = :studentIndex
            """)
    Optional<AccountCredentialsProjection> findCredentialsByStudentIndex(
            @Param("studentIndex") String studentIndex
    );
}
