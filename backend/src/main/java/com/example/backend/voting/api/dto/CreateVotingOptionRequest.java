package com.example.backend.voting.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateVotingOptionRequest(
        @NotNull @Positive Long optionNumber,
        @NotBlank @Size(max = 100) String text
) {
}
