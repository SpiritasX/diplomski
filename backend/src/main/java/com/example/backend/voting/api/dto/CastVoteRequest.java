package com.example.backend.voting.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CastVoteRequest(
        @NotNull
        @Positive
        Long optionNumber
) {}
