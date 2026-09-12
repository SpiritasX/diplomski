package com.example.backend.voting.api;

import com.example.backend.voting.api.dto.*;
import com.example.backend.voting.internal.application.VotingProposalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/{proposalId}")
    @PreAuthorize("hasAuthority('ROLE_REPRESENTATIVE')")
    public VotingProposalResponse updateVotingProposal(
            Authentication authentication,
            @PathVariable UUID proposalId,
            @Valid @RequestBody UpdateVotingProposalRequest request
    ) {
        return votingProposalService.updateVotingProposal(
                proposalId,
                studentIndex(authentication),
                request
        );
    }

    @PostMapping("/{proposalId}/options")
    @PreAuthorize("hasAuthority('ROLE_REPRESENTATIVE')")
    @ResponseStatus(HttpStatus.CREATED)
    public VotingProposalResponse createVotingProposalOptions(
            Authentication authentication,
            @PathVariable UUID proposalId,
            @Valid @RequestBody CreateVotingOptionsRequest request
    ) {
        return votingProposalService.addVotingProposalOptions(
                proposalId,
                studentIndex(authentication),
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
            Authentication authentication,
            @PathVariable UUID proposalId
    ) {
        return votingProposalService.lockProposal(proposalId, studentIndex(authentication));
    }

    @PostMapping("/{proposalId}/cancel")
    @PreAuthorize("hasAuthority('ROLE_REPRESENTATIVE')")
    public VotingProposalResponse cancelProposal(
            Authentication authentication,
            @PathVariable UUID proposalId
    ) {
        return votingProposalService.cancelProposal(proposalId, studentIndex(authentication));
    }

    private static String studentIndex(Authentication authentication) {
        return authentication.getName();
    }
}
