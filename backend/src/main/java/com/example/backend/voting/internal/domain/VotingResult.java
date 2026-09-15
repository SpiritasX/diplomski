package com.example.backend.voting.internal.domain;

import com.example.backend.voting.internal.domain.enums.VotingResultOutcome;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.type.NumericBooleanConverter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "VOTING_RESULTS")
@Getter
public class VotingResult {

    @Id
    @Column(name = "VOTING_PROPOSAL_ID", nullable = false)
    private UUID votingProposalId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "VOTING_PROPOSAL_ID", nullable = false, updatable = false)
    private VotingProposal votingProposal;

    @Column(name = "ELIGIBLE_COUNT", nullable = false)
    private Long eligibleCount;

    @Column(name = "PARTICIPATION_COUNT", nullable = false)
    private Long participationCount;

    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "QUORUM_MET", nullable = false)
    private Boolean quorumMet;

    @Enumerated(EnumType.STRING)
    @Column(name = "OUTCOME", nullable = false, length = 20)
    private VotingResultOutcome outcome;

    @Column(name = "COMPUTED_AT", nullable = false)
    private OffsetDateTime computedAt;

    @Column(name = "PUBLISHED_AT")
    private OffsetDateTime publishedAt;

    @Column(name = "RESULT_HASH", nullable = false, length = 64)
    private String resultHash;

    protected VotingResult() {
    }

    public VotingResult(
            VotingProposal votingProposal,
            Long eligibleCount,
            Long participationCount,
            Boolean quorumMet,
            VotingResultOutcome outcome,
            OffsetDateTime computedAt,
            String resultHash
    ) {
        this.votingProposal = Objects.requireNonNull(votingProposal, "votingProposal must not be null");
        this.votingProposalId = votingProposal.getVotingProposalId();
        this.eligibleCount = Objects.requireNonNull(eligibleCount, "eligibleCount must not be null");
        this.participationCount = Objects.requireNonNull(participationCount, "participationCount must not be null");
        this.quorumMet = Objects.requireNonNull(quorumMet, "quorumMet must not be null");
        this.outcome = Objects.requireNonNull(outcome, "outcome must not be null");
        this.computedAt = Objects.requireNonNull(computedAt, "computedAt must not be null");
        this.resultHash = Objects.requireNonNull(resultHash, "resultHash must not be null");
    }
}
