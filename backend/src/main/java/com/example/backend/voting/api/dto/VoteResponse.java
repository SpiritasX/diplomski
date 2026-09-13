package com.example.backend.voting.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VoteResponse(
        UUID proposalId,
        OffsetDateTime votedAt,
        String receipt
) {
}
