package com.example.backend.voting.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
public class EligibleVoterId implements Serializable {

    @Column(name = "VOTING_PROPOSAL_ID", nullable = false)
    private UUID votingProposalId;

    @Column(name = "STUDENT_INDEX", nullable = false, length = 20)
    private String studentIndex;

    protected EligibleVoterId() {
    }

    public EligibleVoterId(UUID votingProposalId, String studentIndex) {
        this.votingProposalId = Objects.requireNonNull(votingProposalId, "votingProposalId must not be null");
        this.studentIndex = Objects.requireNonNull(studentIndex, "studentIndex must not be null");
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof EligibleVoterId that)) {
            return false;
        }

        return Objects.equals(votingProposalId, that.votingProposalId)
                && Objects.equals(studentIndex, that.studentIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(votingProposalId, studentIndex);
    }
}
