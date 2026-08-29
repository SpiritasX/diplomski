package com.example.backend.voting.internal.domain;

import com.example.backend.voting.internal.domain.enums.BallotType;
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

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "BALLOT_TYPE", nullable = false, length = 10)
    private BallotType ballotType;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 10)
    private VotingProposalStatus status;

    @Column(name = "STARTS_AT", nullable = false)
    private OffsetDateTime startsAt;

    @Column(name = "ENDS_AT", nullable = false)
    private OffsetDateTime endsAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "QUORUM_TYPE", nullable = false, length = 15)
    private QuorumType quorumType;

    @Column(name = "QUORUM_VALUE")
    private Long quorumValue;

    @Column(name = "DECISION_RULE", length = 50)
    private String decisionRule;

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
            String title,
            String description,
            BallotType ballotType,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            QuorumType quorumType,
            Long quorumValue,
            String decisionRule,
            OffsetDateTime createdAt
    ) {
        this.votingProposalId = UUID.randomUUID();
        this.creatorStudentIndex = Objects.requireNonNull(creatorStudentIndex, "creatorStudentIndex must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.description = description;
        this.ballotType = Objects.requireNonNull(ballotType, "ballotType must not be null");
        this.status = VotingProposalStatus.DRAFT;
        this.startsAt = Objects.requireNonNull(startsAt, "startsAt must not be null");
        this.endsAt = Objects.requireNonNull(endsAt, "endsAt must not be null");
        this.quorumType = Objects.requireNonNull(quorumType, "quorumType must not be null");
        this.quorumValue = quorumValue;
        this.decisionRule = decisionRule;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public boolean isDraft() {
        return status == VotingProposalStatus.DRAFT;
    }

    public boolean isLocked() {
        return status == VotingProposalStatus.LOCKED;
    }

    public void setBallotType(BallotType ballotType) {
        if (!isDraft()) {
            return;
        }

        this.ballotType = Objects.requireNonNull(ballotType, "ballotType must not be null");
    }

    public void lock(OffsetDateTime lockedAt, String configurationHash) {
        if (!isDraft()) {
            return;
        }

        this.status = VotingProposalStatus.LOCKED;
        this.lockedAt = Objects.requireNonNull(lockedAt, "lockedAt must not be null");
        this.configurationHash = Objects.requireNonNull(configurationHash, "configurationHash must not be null");
    }
}
