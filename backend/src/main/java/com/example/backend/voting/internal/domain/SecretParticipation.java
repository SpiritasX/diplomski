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
@Table(name = "SECRET_PARTICIPATIONS")
@Getter
public class SecretParticipation {

    @EmbeddedId
    private SecretParticipationId secretParticipationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("votingProposalId")
    @JoinColumn(name = "VOTING_PROPOSAL_ID", nullable = false, updatable = false)
    private VotingProposal votingProposal;

    @Column(name = "RECORDED_AT", nullable = false, updatable = false)
    private OffsetDateTime recordedAt;

    protected SecretParticipation() {
    }

    public SecretParticipation(VotingProposal votingProposal, String studentIndex, OffsetDateTime recordedAt) {
        Objects.requireNonNull(votingProposal, "votingProposal must not be null");

        this.secretParticipationId = new SecretParticipationId(
                votingProposal.getVotingProposalId(),
                Objects.requireNonNull(studentIndex, "studentIndex must not be null")
        );
        this.votingProposal = votingProposal;
        this.recordedAt = Objects.requireNonNull(recordedAt, "recordedAt must not be null");
    }

    public String getStudentIndex() {
        return secretParticipationId.getStudentIndex();
    }
}
