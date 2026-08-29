package com.example.backend.voting.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
public class VotingOptionId implements Serializable {

    @Column(name = "VOTING_PROPOSAL_ID", nullable = false)
    private UUID votingProposalId;

    @Column(name = "VOTING_OPTION_NUMBER", nullable = false)
    private Long votingOptionNumber;

    protected VotingOptionId() {
    }

    public VotingOptionId(UUID votingProposalId, Long votingOptionNumber) {
        this.votingProposalId = Objects.requireNonNull(votingProposalId, "votingProposalId must not be null");
        this.votingOptionNumber = Objects.requireNonNull(votingOptionNumber, "votingOptionNumber must not be null");
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof VotingOptionId that)) {
            return false;
        }

        return Objects.equals(votingProposalId, that.votingProposalId)
                && Objects.equals(votingOptionNumber, that.votingOptionNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(votingProposalId, votingOptionNumber);
    }
}
