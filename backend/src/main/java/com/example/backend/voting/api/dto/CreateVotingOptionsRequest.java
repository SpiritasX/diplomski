package com.example.backend.voting.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateVotingOptionsRequest(
        @NotEmpty List<@Valid CreateVotingOptionRequest> options
) {
}
