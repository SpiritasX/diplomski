package com.example.backend.voting.api;

// TODO generate results: count -> quorum -> decision rule -> result

import com.example.backend.voting.api.dto.CastVoteRequest;
import com.example.backend.voting.api.dto.VoteResponse;
import com.example.backend.voting.internal.application.VoteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(
        name = "Votes",
        description = "Voting proposal vote casting"
)
@RestController
@RequestMapping("/voting/proposals/{proposalId}/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping
    @PreAuthorize("!hasAnyAuthority('ROLE_ADMIN', 'ROLE_REPRESENTATIVE')")
    public VoteResponse vote(
            Authentication authentication,
            @PathVariable UUID proposalId,
            @Valid @RequestBody CastVoteRequest request
    ) {
        return voteService.vote(
                studentIndex(authentication),
                proposalId,
                request
        );
    }

    private static String studentIndex(Authentication authentication) {
        return authentication.getName();
    }
}
