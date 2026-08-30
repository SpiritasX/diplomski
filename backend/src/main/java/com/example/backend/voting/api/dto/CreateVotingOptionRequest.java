package com.example.backend.voting.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateVotingOptionRequest(
        @Schema(example = "1")
        @NotNull
        @Positive
        Long optionNumber,

        @Schema(example = "For")
        @NotBlank
        @Size(max = 100)
        String text
) {
}
