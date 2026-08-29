package com.example.backend.voting.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

import java.util.Objects;

@Entity
@Table(
        name = "VOTING_OPTIONS",
        uniqueConstraints = @UniqueConstraint(
                name = "UQ_VOTING_OPTION_PROPOSAL_TEXT",
                columnNames = {"VOTING_PROPOSAL_ID", "TEXT"}
        )
)
@Getter
public class VotingOption {

    @EmbeddedId
    private VotingOptionId votingOptionId;

    @Column(name = "TEXT", nullable = false, length = 100)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("votingProposalId")
    @JoinColumn(name = "VOTING_PROPOSAL_ID", nullable = false, updatable = false)
    private VotingProposal votingProposal;

    protected VotingOption() {
    }

    public VotingOption(Long votingOptionNumber, String text, VotingProposal votingProposal) {
        Objects.requireNonNull(votingProposal, "votingProposal cannot be null");

        this.votingOptionId = new VotingOptionId(
                votingProposal.getVotingProposalId(),
                Objects.requireNonNull(votingOptionNumber, "votingOptionNumber cannot be null")
        );
        this.text = Objects.requireNonNull(text, "text cannot be null");
        this.votingProposal = votingProposal;
    }

    public Long getVotingOptionNumber() {
        return votingOptionId.getVotingOptionNumber();
    }

    public void setText(String text) {
        this.text = Objects.requireNonNull(text, "text cannot be null");
    }
}
