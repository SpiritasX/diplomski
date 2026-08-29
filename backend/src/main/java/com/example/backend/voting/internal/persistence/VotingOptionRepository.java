package com.example.backend.voting.internal.persistence;

import com.example.backend.voting.internal.domain.VotingOption;
import com.example.backend.voting.internal.domain.VotingOptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VotingOptionRepository extends JpaRepository<VotingOption, VotingOptionId> {

    @Query("""
            SELECT option
            FROM VotingOption option
            WHERE option.votingProposal.votingProposalId = :proposalId
            ORDER BY option.votingOptionId.votingOptionNumber
            """)
    List<VotingOption> findByProposalId(
            @Param("proposalId") UUID proposalId
    );
}
