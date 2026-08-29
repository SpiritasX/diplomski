package com.example.backend.voting.api.dto;

import java.util.UUID;

public record VotingOptionResponse(
        UUID proposalId,
        Long optionNumber,
        String text
) {
}
