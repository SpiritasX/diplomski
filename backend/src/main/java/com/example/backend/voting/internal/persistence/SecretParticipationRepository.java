package com.example.backend.voting.internal.persistence;

import com.example.backend.voting.internal.domain.SecretParticipation;
import com.example.backend.voting.internal.domain.SecretParticipationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SecretParticipationRepository extends JpaRepository<SecretParticipation, SecretParticipationId> {

    @Query("""
            SELECT COUNT(participation)
            FROM SecretParticipation participation
            WHERE participation.secretParticipationId.votingProposalId = :proposalId
            """)
    long countSecretParticipationByProposalId(
            @Param("proposalId") UUID proposalId
    );
}
