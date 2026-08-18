package com.example.backend.identity.internal.persistence;

import com.example.backend.identity.internal.domain.AdminMandate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface AdminMandateRepository extends JpaRepository<AdminMandate, UUID> {

    default boolean existsActiveMandateForStudent(
            String studentIndex,
            OffsetDateTime now
    ) {
        return countActiveMandatesForStudent(studentIndex, now) > 0;
    }

    @Query("""
            SELECT COUNT(m)
            FROM AdminMandate m
            WHERE m.account.studentIndex = :studentIndex
                AND m.validFrom <= :now
                AND (m.validUntil IS NULL OR m.validUntil > :now)
            """)
    long countActiveMandatesForStudent(
            @Param("studentIndex") String studentIndex,
            @Param("now") OffsetDateTime now
    );
}
