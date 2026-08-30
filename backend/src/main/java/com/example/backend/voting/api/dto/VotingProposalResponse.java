package com.example.backend.voting.api.dto;

import com.example.backend.voting.internal.domain.enums.BallotType;
import com.example.backend.voting.internal.domain.enums.DecisionRule;
import com.example.backend.voting.internal.domain.enums.QuorumType;
import com.example.backend.voting.internal.domain.enums.VotingProposalStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record VotingProposalResponse(
        UUID proposalId,
        String creatorStudentIndex,
        UUID creatorMandateId,
        UUID creatorBodyId,
        String title,
        String description,
        BallotType ballotType,
        VotingProposalStatus status,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        QuorumType quorumType,
        Long quorumValue,
        DecisionRule decisionRule,
        String configurationHash,
        OffsetDateTime createdAt,
        OffsetDateTime lockedAt,
        List<VotingOptionResponse> options,
        List<EligibleVoterResponse> eligibleVoters
) {
}
