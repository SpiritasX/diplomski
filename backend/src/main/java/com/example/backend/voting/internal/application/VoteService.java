package com.example.backend.voting.internal.application;

import com.example.backend.config.RefreshTokenHasher;
import com.example.backend.shared.error.BusinessRuleViolationException;
import com.example.backend.shared.error.ResourceNotFoundException;
import com.example.backend.voting.api.dto.CastVoteRequest;
import com.example.backend.voting.api.dto.VoteResponse;
import com.example.backend.voting.internal.domain.EligibleVoter;
import com.example.backend.voting.internal.domain.SecretBallot;
import com.example.backend.voting.internal.domain.SecretBallotId;
import com.example.backend.voting.internal.domain.SecretParticipation;
import com.example.backend.voting.internal.domain.SecretParticipationId;
import com.example.backend.voting.internal.domain.VotingOptionId;
import com.example.backend.voting.internal.domain.VotingProposal;
import com.example.backend.voting.internal.persistence.EligibleVoterRepository;
import com.example.backend.voting.internal.persistence.SecretBallotRepository;
import com.example.backend.voting.internal.persistence.SecretParticipationRepository;
import com.example.backend.voting.internal.persistence.VotingOptionRepository;
import com.example.backend.voting.internal.persistence.VotingProposalRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class VoteService {

    private static final int MAX_RECEIPT_HASH_GENERATION_ATTEMPTS = 10;

    private final VotingProposalRepository votingProposalRepository;
    private final VotingOptionRepository votingOptionRepository;
    private final EligibleVoterRepository eligibleVoterRepository;
    private final SecretBallotRepository secretBallotRepository;
    private final SecretParticipationRepository secretParticipationRepository;
    private final RefreshTokenHasher receiptHasher;
    private final Clock clock;
    private final VotingProposalLifecycleService votingProposalLifecycleService;

    public VoteService(
            VotingProposalLifecycleService votingProposalLifecycleService,
            VotingProposalRepository votingProposalRepository,
            VotingOptionRepository votingOptionRepository,
            EligibleVoterRepository eligibleVoterRepository,
            SecretBallotRepository secretBallotRepository,
            SecretParticipationRepository secretParticipationRepository,
            RefreshTokenHasher receiptHasher,
            Clock clock
    ) {
        this.votingProposalLifecycleService = votingProposalLifecycleService;
        this.votingProposalRepository = votingProposalRepository;
        this.votingOptionRepository = votingOptionRepository;
        this.eligibleVoterRepository = eligibleVoterRepository;
        this.secretBallotRepository = secretBallotRepository;
        this.secretParticipationRepository = secretParticipationRepository;
        this.receiptHasher = receiptHasher;
        this.clock = clock;
    }

    @Transactional
    public VoteResponse vote(String studentIndex, UUID proposalId, @Valid CastVoteRequest request) {
        Long optionNumber = optionNumber(request);
        VotingProposal proposal = votingProposalRepository.findForUpdateById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Voting proposal not found."));
        OffsetDateTime votedAt = now();

        votingProposalLifecycleService.updateProposal(votedAt, proposal);
        assertCurrentlyOpen(proposal, votedAt);

        EligibleVoter voter = eligibleVoterRepository
                .findForUpdateByProposalIdAndStudentIndex(proposalId, studentIndex)
                .orElseThrow(() -> new BusinessRuleViolationException("Student is not eligible to vote on this proposal."));
        SecretParticipationId participationId =
                new SecretParticipationId(proposalId, studentIndex);

        assertNotVoted(voter, participationId);
        assertOptionExists(proposalId, optionNumber);

        return switch (proposal.getBallotType()) {
            case PUBLIC -> castPublicVote(voter, proposal, optionNumber, votedAt);
            case SECRET -> castSecretVote(proposal, optionNumber, votedAt, participationId);
        };
    }

    private VoteResponse castPublicVote(EligibleVoter voter, VotingProposal proposal, Long optionNumber, OffsetDateTime votedAt) {
        voter.recordPublicVote(optionNumber, votedAt);

        return toResponse(proposal, votedAt);
    }

    private VoteResponse castSecretVote(VotingProposal proposal, Long optionNumber, OffsetDateTime votedAt, SecretParticipationId participationId) {
        secretParticipationRepository.save(
                new SecretParticipation(
                        proposal,
                        participationId.getStudentIndex(),
                        votedAt
                )
        );

        ReceiptData receiptData = generateUniqueSecretBallotReceipt(proposal.getVotingProposalId());
        secretBallotRepository.save(
                new SecretBallot(
                        proposal,
                        receiptData.hash(),
                        optionNumber,
                        votedAt
                )
        );

        return toResponse(proposal, votedAt, receiptData.receipt());
    }

    private void assertOptionExists(UUID proposalId, Long optionNumber) {
        if (!votingOptionRepository.existsById(new VotingOptionId(proposalId, optionNumber))) {
            throw new BusinessRuleViolationException("Voting option does not belong to this proposal.");
        }
    }

    private void assertNotVoted(EligibleVoter voter, SecretParticipationId participationId) {
        if (voter.hasVoted() || secretParticipationRepository.existsById(participationId)) {
            throw new BusinessRuleViolationException("Eligible voter has already voted.");
        }
    }

    private ReceiptData generateUniqueSecretBallotReceipt(UUID proposalId) {
        for (int attempt = 0; attempt < MAX_RECEIPT_HASH_GENERATION_ATTEMPTS; attempt++) {
            String receipt = UUID.randomUUID().toString();
            String receiptHash = receiptHasher.hash(receipt);

            if (!secretBallotRepository.existsById(new SecretBallotId(proposalId, receiptHash))) {
                return new ReceiptData(receipt, receiptHash);
            }
        }

        throw new IllegalStateException("Unable to generate unique secret ballot receipt hash.");
    }

    private static void assertCurrentlyOpen(VotingProposal proposal, OffsetDateTime now) {
        if (!proposal.isOpen() || now.isBefore(proposal.getStartsAt()) || !now.isBefore(proposal.getEndsAt())) {
            throw new BusinessRuleViolationException("Voting proposal is not currently open.");
        }
    }

    private static Long optionNumber(CastVoteRequest request) {
        if (request == null || request.optionNumber() == null) {
            throw new BusinessRuleViolationException("Voting option number is required.");
        }

        if (request.optionNumber() < 1) {
            throw new BusinessRuleViolationException("Voting option number must be positive.");
        }

        return request.optionNumber();
    }

    private record ReceiptData(String receipt, String hash) {}

    private OffsetDateTime now() {
        return OffsetDateTime.now(clock);
    }

    private static VoteResponse toResponse(VotingProposal proposal, OffsetDateTime votedAt) {
        return toResponse(proposal, votedAt, null);
    }

    private static VoteResponse toResponse(VotingProposal proposal, OffsetDateTime votedAt, String receipt) {
        return new VoteResponse(
                proposal.getVotingProposalId(),
                votedAt,
                receipt
        );
    }
}
