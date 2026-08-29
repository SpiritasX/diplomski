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
@Table(name = "ELIGIBLE_VOTERS")
@Getter
public class EligibleVoter {

    @EmbeddedId
    private EligibleVoterId eligibleVoterId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("votingProposalId")
    @JoinColumn(name = "VOTING_PROPOSAL_ID", nullable = false, updatable = false)
    private VotingProposal votingProposal;

    @Column(name = "VOTED_AT")
    private OffsetDateTime votedAt;

    @Column(name = "RECEIPT_HASH", unique = true, nullable = false, length = 64)
    private String receiptHash;

    protected EligibleVoter() {
    }

    public EligibleVoter(
            VotingProposal votingProposal,
            String studentIndex,
            String receiptHash
    ) {
        Objects.requireNonNull(votingProposal, "votingProposal must not be null");

        this.eligibleVoterId = new EligibleVoterId(
                votingProposal.getVotingProposalId(),
                Objects.requireNonNull(studentIndex, "studentIndex must not be null")
        );
        this.votingProposal = votingProposal;
        this.receiptHash = Objects.requireNonNull(receiptHash, "receiptHash must not be null");
    }

    public String getStudentIndex() {
        return eligibleVoterId.getStudentIndex();
    }
}
