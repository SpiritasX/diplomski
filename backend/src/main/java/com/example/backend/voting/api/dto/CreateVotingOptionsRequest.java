package com.example.backend.voting.api.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(example = """
        {
          "options": [
            {"optionNumber": 1, "text": "For"},
            {"optionNumber": 2, "text": "Against"}
          ]
        }
        """)
public record CreateVotingOptionsRequest(
        @ArraySchema(
                arraySchema = @Schema(example = """
                        [
                          {"optionNumber": 1, "text": "For"},
                          {"optionNumber": 2, "text": "Against"}
                        ]
                        """)
        )
        @NotEmpty
        List<@Valid CreateVotingOptionRequest> options
) {
}
