package com.example.backend.voting.internal.persistence;

import com.example.backend.voting.internal.domain.EligibleVoter;
import com.example.backend.voting.internal.domain.EligibleVoterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EligibleVoterRepository extends JpaRepository<EligibleVoter, EligibleVoterId> {

    boolean existsByReceiptHash(String receiptHash);

    @Query("""
            SELECT voter
            FROM EligibleVoter voter
            WHERE voter.votingProposal.votingProposalId = :proposalId
            ORDER BY voter.eligibleVoterId.studentIndex
            """)
    List<EligibleVoter> findByProposalId(
            @Param("proposalId") UUID proposalId
    );
}
