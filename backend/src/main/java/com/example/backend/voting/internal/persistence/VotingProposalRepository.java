package com.example.backend.voting.internal.persistence;

import com.example.backend.voting.internal.domain.VotingProposal;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VotingProposalRepository extends JpaRepository<VotingProposal, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT proposal
            FROM VotingProposal proposal
            WHERE proposal.votingProposalId = :proposalId
            """)
    Optional<VotingProposal> findForUpdateById(
            @Param("proposalId") UUID proposalId
    );
}
