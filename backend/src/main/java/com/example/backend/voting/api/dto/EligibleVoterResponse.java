package com.example.backend.voting.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EligibleVoterResponse(
        UUID proposalId,
        String studentIndex,
        OffsetDateTime votedAt,
        String receiptHash
) {
}
