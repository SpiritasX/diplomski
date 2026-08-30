package com.example.backend.voting.api.dto;

import com.example.backend.voting.internal.domain.enums.BallotType;
import com.example.backend.voting.internal.domain.enums.QuorumType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

@Schema(example = """
        {
          "title": "Student parliament budget approval",
          "description": "Approve the proposed student parliament budget for the 2026/2027 academic year.",
          "ballotType": "PUBLIC",
          "startsAt": "2026-09-01T10:00:00Z",
          "endsAt": "2026-09-02T10:00:00Z",
          "quorumType": "ABSOLUTE",
          "quorumValue": 25,
          "decisionRule": "SIMPLE_MAJORITY"
        }
        """)
public record UpdateVotingProposalRequest(
        @Schema(example = "Student parliament budget approval")
        @Size(max = 200)
        String title,

        @Schema(example = "Approve the proposed student parliament budget for the 2026/2027 academic year.")
        @Size(max = 1000)
        String description,

        @Schema(example = "PUBLIC")
        BallotType ballotType,

        @Schema(example = "2026-09-01T10:00:00Z")
        OffsetDateTime startsAt,

        @Schema(example = "2026-09-02T10:00:00Z")
        OffsetDateTime endsAt,

        @Schema(example = "ABSOLUTE")
        QuorumType quorumType,

        @Schema(example = "25")
        @PositiveOrZero
        Long quorumValue,

        @Schema(example = "SIMPLE_MAJORITY")
        @Size(max = 50)
        String decisionRule
) {
}
