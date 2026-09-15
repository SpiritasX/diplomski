package com.example.backend.voting.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.Objects;

@Entity
@Table(name = "VOTING_OPTION_RESULTS")
@Getter
public class VotingOptionResult {

    @EmbeddedId
    private VotingOptionResultId votingOptionResultId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("votingProposalId")
    @JoinColumn(name = "VOTING_PROPOSAL_ID", nullable = false, updatable = false)
    private VotingResult votingResult;

    @Column(name = "VOTE_COUNT", nullable = false)
    private Long voteCount;

    protected VotingOptionResult() {
    }

    public VotingOptionResult(VotingResult votingResult, Long votingOptionNumber, Long voteCount) {
        Objects.requireNonNull(votingResult, "votingResult must not be null");

        this.votingOptionResultId = new VotingOptionResultId(
                votingResult.getVotingProposalId(),
                Objects.requireNonNull(votingOptionNumber, "votingOptionNumber must not be null")
        );
        this.votingResult = votingResult;
        this.voteCount = Objects.requireNonNull(voteCount, "voteCount must not be null");
    }
}
