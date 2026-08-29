package com.example.backend.identity.internal.persistence;

import com.example.backend.identity.internal.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
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

    @Query("""
            SELECT a.studentIndex
            FROM Account a
            WHERE a.enrollmentStatus.code = 'ACTIVE'
                AND NOT EXISTS (
                    SELECT m
                    FROM AdminMandate m
                    WHERE m.account = a
                        AND m.validFrom <= :now
                        AND (m.validUntil IS NULL OR m.validUntil > :now)
                )
                AND NOT EXISTS (
                    SELECT m
                    FROM RepresentativeMandate m
                    WHERE m.account = a
                        AND m.validFrom <= :now
                        AND (m.validUntil IS NULL OR m.validUntil > :now)
                )
            ORDER BY a.studentIndex
            """)
    List<String> findActiveStudentIndexesWithoutActiveMandate(
            @Param("now") OffsetDateTime now
    );
}
