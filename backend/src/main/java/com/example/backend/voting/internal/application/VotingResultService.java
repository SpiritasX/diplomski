package com.example.backend.voting.internal.application;

import com.example.backend.config.RefreshTokenHasher;
import com.example.backend.shared.error.BusinessRuleViolationException;
import com.example.backend.shared.error.ResourceNotFoundException;
import com.example.backend.voting.internal.domain.VotingOption;
import com.example.backend.voting.internal.domain.VotingOptionResult;
import com.example.backend.voting.internal.domain.VotingProposal;
import com.example.backend.voting.internal.domain.VotingResult;
import com.example.backend.voting.internal.domain.enums.DecisionRule;
import com.example.backend.voting.internal.domain.enums.VotingProposalStatus;
import com.example.backend.voting.internal.domain.enums.VotingResultOutcome;
import com.example.backend.voting.internal.persistence.EligibleVoterRepository;
import com.example.backend.voting.internal.persistence.SecretBallotRepository;
import com.example.backend.voting.internal.persistence.SecretParticipationRepository;
import com.example.backend.voting.internal.persistence.VotingOptionRepository;
import com.example.backend.voting.internal.persistence.VotingOptionResultRepository;
import com.example.backend.voting.internal.persistence.VotingOptionVoteCount;
import com.example.backend.voting.internal.persistence.VotingProposalRepository;
import com.example.backend.voting.internal.persistence.VotingResultRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VotingResultService {

    private final VotingProposalRepository votingProposalRepository;
    private final EligibleVoterRepository eligibleVoterRepository;
    private final SecretParticipationRepository secretParticipationRepository;
    private final VotingOptionRepository votingOptionRepository;
    private final SecretBallotRepository secretBallotRepository;
    private final VotingResultRepository votingResultRepository;
    private final RefreshTokenHasher resultHasher;
    private final EntityManager entityManager;

    public VotingResultService(
            VotingProposalRepository votingProposalRepository,
            EligibleVoterRepository eligibleVoterRepository,
            SecretParticipationRepository secretParticipationRepository,
            VotingOptionRepository votingOptionRepository,
            SecretBallotRepository secretBallotRepository,
            VotingResultRepository votingResultRepository,
            RefreshTokenHasher resultHasher,
            EntityManager entityManager
    ) {
        this.votingProposalRepository = votingProposalRepository;
        this.eligibleVoterRepository = eligibleVoterRepository;
        this.secretParticipationRepository = secretParticipationRepository;
        this.votingOptionRepository = votingOptionRepository;
        this.secretBallotRepository = secretBallotRepository;
        this.votingResultRepository = votingResultRepository;
        this.resultHasher = resultHasher;
        this.entityManager = entityManager;
    }

    @Transactional
    public void computeAndStoreResult(UUID proposalId, OffsetDateTime now) {
        VotingProposal proposal = votingProposalRepository.findForUpdateById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Voting proposal not found."));

        computeAndStoreResult(proposal, now);
    }

    @Transactional
    public void computeAndStoreResult(VotingProposal proposal, OffsetDateTime now) {
        UUID proposalId = proposal.getVotingProposalId();

        assertClosed(proposal);
        assertResultNotComputed(proposalId);

        storeResult(proposal, proposalId, now);
    }

    private void storeResult(VotingProposal proposal, UUID proposalId, OffsetDateTime now) {
        long eligibleCount = eligibleVoterRepository.countEligibleVotersByProposalId(proposalId);
        long participationCount = participationCount(proposal);

        boolean quorumMet = isQuorumMet(proposal, eligibleCount, participationCount);

        Map<Long, Long> voteCountsByOptionNumber = quorumMet
                ? voteCountsByOptionNumber(proposal)
                : Map.of();

        VotingResultOutcome outcome = quorumMet
                ? outcome(proposal.getDecisionRule(), participationCount, voteCountsByOptionNumber)
                : VotingResultOutcome.NO_DECISION;

        String resultHash = resultHash(
                proposalId,
                eligibleCount,
                participationCount,
                quorumMet,
                outcome,
                now,
                voteCountsByOptionNumber
        );

        VotingResult result = new VotingResult(
                proposal,
                eligibleCount,
                participationCount,
                quorumMet,
                outcome,
                now,
                resultHash
        );

        // repo.save(...) doesn't work here because of @MapsId
        // which tries to derive the id from votingProposal while
        // hibernate switches to merge instead of persist and the
        // identifier is still null at that point.
        entityManager.persist(result);

        if (quorumMet) {
            voteCountsByOptionNumber.entrySet()
                    .stream()
                    .map(entry -> new VotingOptionResult(
                            result,
                            entry.getKey(),
                            entry.getValue()
                    ))
                    .forEach(entityManager::persist);
        }
    }

    private long participationCount(VotingProposal proposal) {
        UUID proposalId = proposal.getVotingProposalId();

        return switch (proposal.getBallotType()) {
            case PUBLIC -> eligibleVoterRepository.countPublicVotesByProposalId(proposalId);
            case SECRET -> secretParticipationRepository.countSecretParticipationByProposalId(proposalId);
        };
    }

    private Map<Long, Long> voteCountsByOptionNumber(VotingProposal proposal) {
        UUID proposalId = proposal.getVotingProposalId();
        List<VotingOption> options = votingOptionRepository.findByProposalId(proposalId);
        List<VotingOptionVoteCount> countedVotes = switch (proposal.getBallotType()) {
            case PUBLIC -> eligibleVoterRepository.countPublicVotesByProposalIdGroupedByOptionNumber(proposalId);
            case SECRET -> secretBallotRepository.countSecretBallotsByProposalIdGroupedByOptionNumber(proposalId);
        };

        return voteCountsByOptionNumber(options, countedVotes);
    }

    private static Map<Long, Long> voteCountsByOptionNumber(List<VotingOption> options, List<VotingOptionVoteCount> countedVotes) {
        Map<Long, Long> countedVotesByOptionNumber = countedVotes.stream()
                .collect(Collectors.toMap(
                        VotingOptionVoteCount::getOptionNumber,
                        VotingOptionVoteCount::getVoteCount
                ));

        Map<Long, Long> result = new LinkedHashMap<>();

        options.stream()
                .map(VotingOption::getVotingOptionNumber)
                .sorted()
                .forEach(optionNumber -> result.put(
                        optionNumber,
                        countedVotesByOptionNumber.getOrDefault(optionNumber, 0L)
                ));

        return result;
    }

    private static VotingResultOutcome outcome(DecisionRule decisionRule, long participationCount, Map<Long, Long> voteCountsByOptionNumber) {
        if (participationCount == 0) {
            return VotingResultOutcome.NO_DECISION;
        }

        long winningVoteCount = voteCountsByOptionNumber.values()
                .stream()
                .max(Comparator.naturalOrder())
                .orElse(0L);

        if (winningVoteCount == 0) {
            return VotingResultOutcome.NO_DECISION;
        }

        long winningOptions = voteCountsByOptionNumber.values()
                .stream()
                .filter(voteCount -> voteCount == winningVoteCount)
                .count();

        if (winningOptions > 1) {
            return VotingResultOutcome.TIE;
        }

        return switch (decisionRule) {
            case PLURALITY -> VotingResultOutcome.OPTION_SELECTED;
            case SIMPLE_MAJORITY -> winningVoteCount * 2 > participationCount
                    ? VotingResultOutcome.OPTION_SELECTED
                    : VotingResultOutcome.NO_DECISION;
            case UNANIMITY -> winningVoteCount == participationCount
                    ? VotingResultOutcome.OPTION_SELECTED
                    : VotingResultOutcome.NO_DECISION;
        };
    }

    private static boolean isQuorumMet(VotingProposal proposal, long eligibleCount, long participationCount) {
        return switch (proposal.getQuorumType()) {
            case NONE -> true;
            case ABSOLUTE -> participationCount >= proposal.getQuorumValue();
            case PERCENTAGE -> participationCount * 100 >= eligibleCount * proposal.getQuorumValue();
        };
    }

    private static void assertClosed(VotingProposal proposal) {
        if (proposal.getStatus() != VotingProposalStatus.CLOSED) {
            throw new BusinessRuleViolationException("Voting result can only be computed for a closed proposal.");
        }
    }

    private void assertResultNotComputed(UUID proposalId) {
        if (votingResultRepository.existsById(proposalId)) {
            throw new BusinessRuleViolationException("Voting result has already been computed.");
        }
    }

    private String resultHash(
            UUID proposalId,
            long eligibleCount,
            long participationCount,
            boolean quorumMet,
            VotingResultOutcome outcome,
            OffsetDateTime computedAt,
            Map<Long, Long> voteCountsByOptionNumber
    ) {
        StringBuilder builder = new StringBuilder();

        appendHashField(builder, "voting_proposal_id", proposalId);
        appendHashField(builder, "eligible_count", eligibleCount);
        appendHashField(builder, "participation_count", participationCount);
        appendHashField(builder, "quorum_met", quorumMet);
        appendHashField(builder, "outcome", outcome.name());
        appendHashField(builder, "computed_at", computedAt.toInstant().toString());

        voteCountsByOptionNumber.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> appendHashField(
                        builder,
                        "option_result",
                        entry.getKey() + ":" + entry.getValue()
                ));

        return resultHasher.hash(builder.toString());
    }

    private static void appendHashField(StringBuilder builder, String name, Object value) {
        String normalizedValue = Objects.toString(value, "");

        builder.append(name.length())
                .append(':')
                .append(name)
                .append('=')
                .append(normalizedValue.length())
                .append(':')
                .append(normalizedValue)
                .append('\n');
    }
}
