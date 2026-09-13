package com.example.backend.voting.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
public class SecretBallotId implements Serializable {

    @Column(name = "VOTING_PROPOSAL_ID", nullable = false)
    private UUID votingProposalId;

    @Column(name = "RECEIPT_HASH", nullable = false, length = 64)
    private String receiptHash;

    protected SecretBallotId() {
    }

    public SecretBallotId(UUID votingProposalId, String receiptHash) {
        this.votingProposalId = Objects.requireNonNull(votingProposalId, "votingProposalId must not be null");
        this.receiptHash = Objects.requireNonNull(receiptHash, "receipt must not be null");
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof SecretBallotId that)) {
            return false;
        }

        return Objects.equals(votingProposalId, that.votingProposalId) && Objects.equals(receiptHash, that.receiptHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(votingProposalId, receiptHash);
    }
}
