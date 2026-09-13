package com.example.backend.voting.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "SECRET_BALLOTS")
@Getter
public class SecretBallot {

    @EmbeddedId
    private SecretBallotId secretBallotId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("votingProposalId")
    @JoinColumn(name = "VOTING_PROPOSAL_ID", nullable = false, updatable = false)
    private VotingProposal votingProposal;

    @Column(name = "VOTING_OPTION_NUMBER", nullable = false, updatable = false)
    private Long votingOptionNumber;

    @Column(name = "CAST_AT", nullable = false, updatable = false)
    private OffsetDateTime castAt;

    protected SecretBallot() {
    }

    public SecretBallot(VotingProposal votingProposal, String receiptHash, Long votingOptionNumber, OffsetDateTime castAt) {
        Objects.requireNonNull(votingProposal, "votingProposal must not be null");

        this.secretBallotId = new SecretBallotId(
                votingProposal.getVotingProposalId(),
                Objects.requireNonNull(receiptHash, "receipt must not be null")
        );
        this.votingProposal = votingProposal;
        this.votingOptionNumber = Objects.requireNonNull(votingOptionNumber, "votingOptionNumber must not be null");
        this.castAt = Objects.requireNonNull(castAt, "castAt must not be null");
    }

    public String getReceiptHash() {
        return secretBallotId.getReceiptHash();
    }
}
