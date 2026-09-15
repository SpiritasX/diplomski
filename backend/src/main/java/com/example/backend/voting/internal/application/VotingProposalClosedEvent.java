package com.example.backend.voting.internal.application;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record VotingProposalClosedEvent(
        UUID proposalId,
        OffsetDateTime closedAt
) {

    public VotingProposalClosedEvent {
        Objects.requireNonNull(proposalId, "proposalId must not be null");
        Objects.requireNonNull(closedAt, "closedAt must not be null");
    }
}
