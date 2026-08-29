package com.example.backend.identity.internal.persistence;

import com.example.backend.identity.internal.domain.RepresentativeMandate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface RepresentativeMandateRepository extends JpaRepository<RepresentativeMandate, UUID> {

    default boolean existsActiveMandateForStudent(
            String studentIndex,
            OffsetDateTime now
    ) {
        return countActiveMandatesForStudent(studentIndex, now) > 0;
    }

    default boolean existsActiveMandateForStudentAndBody(
            String studentIndex,
            UUID bodyId,
            OffsetDateTime now
    ) {
        return countActiveMandatesForStudentAndBody(studentIndex, bodyId, now) > 0;
    }

    @Query("""
            SELECT COUNT(m)
            FROM RepresentativeMandate m
            WHERE m.account.studentIndex = :studentIndex
                AND m.validFrom <= :now
                AND (m.validUntil IS NULL OR m.validUntil > :now)
            """)
    long countActiveMandatesForStudent(
            @Param("studentIndex") String studentIndex,
            @Param("now") OffsetDateTime now
    );

    @Query("""
            SELECT COUNT(m)
            FROM RepresentativeMandate m
            WHERE m.account.studentIndex = :studentIndex
                AND m.body.bodyId = :bodyId
                AND m.validFrom <= :now
                AND (m.validUntil IS NULL OR m.validUntil > :now)
            """)
    long countActiveMandatesForStudentAndBody(
            @Param("studentIndex") String studentIndex,
            @Param("bodyId") UUID bodyId,
            @Param("now") OffsetDateTime now
    );
}
