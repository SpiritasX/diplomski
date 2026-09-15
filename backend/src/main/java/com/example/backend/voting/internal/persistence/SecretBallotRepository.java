package com.example.backend.voting.internal.persistence;

import com.example.backend.voting.internal.domain.SecretBallot;
import com.example.backend.voting.internal.domain.SecretBallotId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SecretBallotRepository extends JpaRepository<SecretBallot, SecretBallotId> {

    @Query("""
            SELECT
                ballot.votingOptionNumber AS optionNumber,
                COUNT(ballot) AS voteCount
            FROM SecretBallot ballot
            WHERE ballot.secretBallotId.votingProposalId = :proposalId
            GROUP BY ballot.votingOptionNumber
            """)
    List<VotingOptionVoteCount> countSecretBallotsByProposalIdGroupedByOptionNumber(
            @Param("proposalId") UUID proposalId
    );
}
