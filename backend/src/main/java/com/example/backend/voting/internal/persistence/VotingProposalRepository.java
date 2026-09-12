package com.example.backend.voting.internal.persistence;

import com.example.backend.voting.internal.domain.VotingProposal;
import com.example.backend.voting.internal.domain.enums.VotingProposalStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT proposal
        FROM VotingProposal proposal
        WHERE
            (
                proposal.status = :lockedStatus
                AND proposal.startsAt <= :now
            )
            OR
            (
                proposal.status = :openStatus
                AND proposal.endsAt <= :now
            )
        """)
    List<VotingProposal> findDueForLifecycleUpdate(
            @Param("lockedStatus") VotingProposalStatus lockedStatus,
            @Param("openStatus") VotingProposalStatus openStatus,
            @Param("now") OffsetDateTime now
    );
}
