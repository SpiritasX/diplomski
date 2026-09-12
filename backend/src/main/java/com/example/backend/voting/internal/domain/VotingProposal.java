package com.example.backend.voting.internal.domain;

import com.example.backend.voting.internal.domain.enums.BallotType;
import com.example.backend.voting.internal.domain.enums.DecisionRule;
import com.example.backend.voting.internal.domain.enums.QuorumType;
import com.example.backend.voting.internal.domain.enums.VotingProposalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "VOTING_PROPOSALS")
@Getter
public class VotingProposal {

    @Id
    @Column(name = "VOTING_PROPOSAL_ID", unique = true, nullable = false)
    private UUID votingProposalId;

    @Column(name = "CREATOR_STUDENT_INDEX", nullable = false, length = 20, updatable = false)
    private String creatorStudentIndex;

    @Column(name = "CREATOR_MANDATE_ID", nullable = false, length = 16, updatable = false)
    private UUID creatorMandateId;

    @Column(name = "CREATOR_BODY_ID", nullable = false, length = 16, updatable = false)
    private UUID creatorBodyId;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "BALLOT_TYPE", length = 10)
    private BallotType ballotType;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 10)
    private VotingProposalStatus status;

    @Column(name = "STARTS_AT")
    private OffsetDateTime startsAt;

    @Column(name = "ENDS_AT")
    private OffsetDateTime endsAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "QUORUM_TYPE", length = 15)
    private QuorumType quorumType;

    @Column(name = "QUORUM_VALUE")
    private Long quorumValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "DECISION_RULE", length = 20)
    private DecisionRule decisionRule;

    @Column(name = "CREATED_AT", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "LOCKED_AT")
    private OffsetDateTime lockedAt;

    @Column(name = "CONFIGURATION_HASH", length = 64)
    private String configurationHash;


    protected VotingProposal() {
    }

    public VotingProposal(
            String creatorStudentIndex,
            UUID creatorMandateId,
            UUID creatorBodyId,
            String title,
            String description,
            OffsetDateTime createdAt
    ) {
        this.votingProposalId = UUID.randomUUID();
        this.creatorStudentIndex = Objects.requireNonNull(creatorStudentIndex, "creatorStudentIndex must not be null");
        this.creatorMandateId = Objects.requireNonNull(creatorMandateId, "creatorMandateId must not be null");
        this.creatorBodyId = Objects.requireNonNull(creatorBodyId, "creatorBodyId must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.description = description;
        this.status = VotingProposalStatus.DRAFT;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public boolean isDraft() {
        return status == VotingProposalStatus.DRAFT;
    }

    public boolean isLocked() {
        return status == VotingProposalStatus.LOCKED;
    }

    public boolean isOpen() {
        return status == VotingProposalStatus.OPEN;
    }

    public void setTitle(String title) {
        if (!isDraft()) {
            return;
        }

        this.title = Objects.requireNonNull(title, "title must not be null");
    }

    public void setDescription(String description) {
        if (!isDraft()) {
            return;
        }

        this.description = description;
    }

    public void setBallotType(BallotType ballotType) {
        if (!isDraft()) {
            return;
        }

        this.ballotType = Objects.requireNonNull(ballotType, "ballotType must not be null");
    }

    public void setStartsAt(OffsetDateTime startsAt) {
        if (!isDraft()) {
            return;
        }

        this.startsAt = Objects.requireNonNull(startsAt, "startsAt must not be null");
    }

    public void setEndsAt(OffsetDateTime endsAt) {
        if (!isDraft()) {
            return;
        }

        this.endsAt = Objects.requireNonNull(endsAt, "endsAt must not be null");
    }

    public void setQuorumType(QuorumType quorumType) {
        if (!isDraft()) {
            return;
        }

        this.quorumType = Objects.requireNonNull(quorumType, "quorumType must not be null");
    }

    public void setQuorumValue(Long quorumValue) {
        if (!isDraft()) {
            return;
        }

        this.quorumValue = quorumValue;
    }

    public void setDecisionRule(DecisionRule decisionRule) {
        if (!isDraft()) {
            return;
        }

        this.decisionRule = decisionRule;
    }

    public void lock(OffsetDateTime lockedAt, String configurationHash) {
        if (!isDraft()) {
            return;
        }

        this.status = VotingProposalStatus.LOCKED;
        this.lockedAt = Objects.requireNonNull(lockedAt, "lockedAt must not be null");
        this.configurationHash = Objects.requireNonNull(configurationHash, "configurationHash must not be null");
    }

    public void cancel() {
        if (!isDraft() && !isLocked()) {
            return;
        }

        this.status = VotingProposalStatus.CANCELLED;
    }

    public void open() {
        if (!isLocked()) {
            return;
        }

        this.status = VotingProposalStatus.OPEN;
    }

    public void close() {
        if (!isLocked() && !isOpen()) {
            return;
        }

        this.status = VotingProposalStatus.CLOSED;
    }
}
