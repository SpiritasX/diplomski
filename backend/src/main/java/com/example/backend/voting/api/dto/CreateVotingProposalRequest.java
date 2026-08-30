package com.example.backend.voting.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(example = """
        {
          "title": "Student parliament budget approval",
          "description": "Approve the proposed student parliament budget for the 2026/2027 academic year."
        }
        """)
public record CreateVotingProposalRequest(
        @Schema(example = "Student parliament budget approval")
        @NotBlank
        @Size(max = 200)
        String title,

        @Schema(example = "Approve the proposed student parliament budget for the 2026/2027 academic year.")
        @Size(max = 1000)
        String description
) {
}
