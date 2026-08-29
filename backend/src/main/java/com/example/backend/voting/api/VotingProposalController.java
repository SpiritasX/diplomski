package com.example.backend.voting.api;

import com.example.backend.voting.api.dto.*;
import com.example.backend.voting.internal.application.VotingProposalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Voting proposals",
        description = "Voting proposal configuration"
)
@RestController
@RequestMapping("/voting/proposals")
public class VotingProposalController {

    private final VotingProposalService votingProposalService;

    public VotingProposalController(VotingProposalService votingProposalService) {
        this.votingProposalService = votingProposalService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_REPRESENTATIVE')")
    @ResponseStatus(HttpStatus.CREATED)
    public VotingProposalResponse createVotingProposal(
            Authentication authentication,
            @Valid @RequestBody CreateVotingProposalRequest request
    ) {
        return votingProposalService.createVotingProposal(
                studentIndex(authentication),
                request
        );
    }

    @GetMapping("/{proposalId}")
    public VotingProposalResponse getVotingProposal(@PathVariable UUID proposalId) {
        return votingProposalService.getVotingProposal(proposalId);
    }

    @PostMapping("/{proposalId}/options")
    @PreAuthorize("hasAuthority('ROLE_REPRESENTATIVE')")
    @ResponseStatus(HttpStatus.CREATED)
    public VotingProposalResponse createVotingProposalOptions(
            @PathVariable UUID proposalId,
            @Valid @RequestBody CreateVotingOptionsRequest request
    ) {
        return votingProposalService.addVotingProposalOptions(
                proposalId,
                request
        );
    }

    @GetMapping("/{proposalId}/options")
    public List<VotingOptionResponse> getVotingProposalOptions(
            @PathVariable UUID proposalId
    ) {
        return votingProposalService.getVotingProposalOptions(proposalId);
    }

    @GetMapping("/{proposalId}/eligible-voters")
    public List<EligibleVoterResponse> getEligibleVoters(
            @PathVariable UUID proposalId
    ) {
        return votingProposalService.getEligibleVoters(proposalId);
    }

    @PostMapping("/{proposalId}/lock")
    @PreAuthorize("hasAuthority('ROLE_REPRESENTATIVE')")
    public VotingProposalResponse lockProposal(
            @PathVariable UUID proposalId
    ) {
        return votingProposalService.lockProposal(proposalId);
    }

    private static String studentIndex(Authentication authentication) {
        return authentication.getName();
    }
}
