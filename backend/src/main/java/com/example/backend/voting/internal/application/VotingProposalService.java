package com.example.backend.voting.internal.application;

import com.example.backend.config.RefreshTokenGenerator;
import com.example.backend.config.RefreshTokenHasher;
import com.example.backend.identity.IdentityAccess;
import com.example.backend.shared.error.BusinessRuleViolationException;
import com.example.backend.shared.error.ResourceNotFoundException;
import com.example.backend.voting.api.dto.CreateVotingOptionRequest;
import com.example.backend.voting.api.dto.CreateVotingOptionsRequest;
import com.example.backend.voting.api.dto.CreateVotingProposalRequest;
import com.example.backend.voting.api.dto.EligibleVoterResponse;
import com.example.backend.voting.api.dto.UpdateVotingProposalRequest;
import com.example.backend.voting.api.dto.VotingOptionResponse;
import com.example.backend.voting.api.dto.VotingProposalResponse;
import com.example.backend.voting.internal.domain.EligibleVoter;
import com.example.backend.voting.internal.domain.EligibleVoterId;
import com.example.backend.voting.internal.domain.VotingOption;
import com.example.backend.voting.internal.domain.VotingOptionId;
import com.example.backend.voting.internal.domain.VotingProposal;
import com.example.backend.voting.internal.domain.enums.BallotType;
import com.example.backend.voting.internal.domain.enums.DecisionRule;
import com.example.backend.voting.internal.domain.enums.QuorumType;
import com.example.backend.voting.internal.persistence.EligibleVoterRepository;
import com.example.backend.voting.internal.persistence.VotingOptionRepository;
import com.example.backend.voting.internal.persistence.VotingProposalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class VotingProposalService {

    private static final int MAX_RECEIPT_HASH_GENERATION_ATTEMPTS = 10;

    private final VotingProposalRepository votingProposalRepository;
    private final VotingOptionRepository votingOptionRepository;
    private final EligibleVoterRepository eligibleVoterRepository;
    private final IdentityAccess identityAccess;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenHasher refreshTokenHasher;
    private final Clock clock;

    public VotingProposalService(
            VotingProposalRepository votingProposalRepository,
            VotingOptionRepository votingOptionRepository,
            EligibleVoterRepository eligibleVoterRepository,
            IdentityAccess identityAccess,
            RefreshTokenGenerator refreshTokenGenerator,
            RefreshTokenHasher refreshTokenHasher,
            Clock clock
    ) {
        this.votingProposalRepository = votingProposalRepository;
        this.votingOptionRepository = votingOptionRepository;
        this.eligibleVoterRepository = eligibleVoterRepository;
        this.identityAccess = identityAccess;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenHasher = refreshTokenHasher;
        this.clock = clock;
    }

    @Transactional
    public VotingProposalResponse createVotingProposal(
            String creatorStudentIndex,
            CreateVotingProposalRequest request
    ) {
        OffsetDateTime now = now();
        IdentityAccess.ActiveMandate mandate = identityAccess.findActiveMandate(creatorStudentIndex, now)
                .orElseThrow(() -> new BusinessRuleViolationException("Active representative mandate is required to create a voting proposal."));

        VotingProposal proposal = votingProposalRepository.save(
                new VotingProposal(
                        creatorStudentIndex,
                        mandate.mandateId(),
                        mandate.bodyId(),
                        normalizedRequiredText(request.title(), "Voting proposal title is required."),
                        normalizedOptionalText(request.description()),
                        now
                )
        );

        return toResponse(proposal, List.of(), List.of());
    }

    @Transactional(readOnly = true)
    public VotingProposalResponse getVotingProposal(UUID proposalId) {
        VotingProposal proposal = findProposal(proposalId);

        return toResponse(
                proposal,
                votingOptionRepository.findByProposalId(proposalId),
                eligibleVoterRepository.findByProposalId(proposalId)
        );
    }

    @Transactional
    public VotingProposalResponse updateVotingProposal(
            UUID proposalId,
            String editorStudentIndex,
            UpdateVotingProposalRequest request
    ) {
        VotingProposal proposal = findProposalForUpdate(proposalId);

        assertEditorAccess(proposal, editorStudentIndex);
        assertDraft(proposal);

        if (request.title() != null) {
            proposal.setTitle(
                    normalizedRequiredText(
                            request.title(),
                            "Voting proposal title is required."
                    )
            );
        }

        if (request.description() != null) {
            proposal.setDescription(normalizedOptionalText(request.description()));
        }

        if (request.ballotType() != null) {
            proposal.setBallotType(request.ballotType());
        }

        if (request.startsAt() != null) {
            proposal.setStartsAt(request.startsAt());
        }

        if (request.endsAt() != null) {
            proposal.setEndsAt(request.endsAt());
        }

        if (request.quorumType() != null) {
            proposal.setQuorumType(request.quorumType());
        }

        if (request.quorumValue() != null) {
            proposal.setQuorumValue(request.quorumValue());
        }

        if (request.decisionRule() != null) {
            proposal.setDecisionRule(request.decisionRule());
        }

        return toResponse(
                proposal,
                votingOptionRepository.findByProposalId(proposalId),
                List.of()
        );
    }

    @Transactional
    public VotingProposalResponse addVotingProposalOptions(
            UUID proposalId,
            String editorStudentIndex,
            CreateVotingOptionsRequest request
    ) {
        VotingProposal proposal = findProposalForUpdate(proposalId);

        assertEditorAccess(proposal, editorStudentIndex);
        assertDraft(proposal);

        List<VotingOption> existingOptions = votingOptionRepository.findByProposalId(proposalId);
        List<ProposalOption> newOptions = normalizeOptions(request.options());

        assertNoOptionConflicts(existingOptions, newOptions);

        List<VotingOption> savedOptions = votingOptionRepository.saveAll(
                newOptions.stream()
                        .map(option -> new VotingOption(
                                option.optionNumber(),
                                option.text(),
                                proposal
                        ))
                        .toList()
        );

        List<VotingOption> responseOptions = Stream
                .concat(existingOptions.stream(), savedOptions.stream())
                .sorted(Comparator.comparing(VotingOption::getVotingOptionNumber))
                .toList();

        return toResponse(proposal, responseOptions, List.of());
    }

    @Transactional(readOnly = true)
    public List<VotingOptionResponse> getVotingProposalOptions(UUID proposalId) {
        assertProposalExists(proposalId);

        return votingOptionRepository.findByProposalId(proposalId)
                .stream()
                .map(VotingProposalService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EligibleVoterResponse> getEligibleVoters(UUID proposalId) {
        assertProposalExists(proposalId);

        return eligibleVoterRepository.findByProposalId(proposalId)
                .stream()
                .map(VotingProposalService::toResponse)
                .toList();
    }

    @Transactional
    public VotingProposalResponse lockProposal(UUID proposalId, String editorStudentIndex) {
        VotingProposal proposal = findProposalForUpdate(proposalId);

        assertEditorAccess(proposal, editorStudentIndex);
        assertDraft(proposal);

        List<VotingOption> options = votingOptionRepository.findByProposalId(proposalId);

        if (options.size() < 2) {
            throw new BusinessRuleViolationException("Voting proposal must have at least two options before locking.");
        }

        validateProposalBeforeLock(proposal);

        OffsetDateTime lockedAt = now();
        List<String> eligibleStudentIndexes = identityAccess.findEligibleVoterStudentIndexes(lockedAt);

        if (eligibleStudentIndexes.isEmpty()) {
            throw new BusinessRuleViolationException("Voting proposal must have at least one eligible voter before locking.");
        }

        Long quorumValue = normalizedQuorumValue(proposal.getQuorumType(), proposal.getQuorumValue());
        proposal.setQuorumValue(quorumValue);

        String configurationHash = configurationHash(
                proposal,
                options.stream()
                        .map(VotingProposalService::toProposalOption)
                        .toList(),
                eligibleStudentIndexes
        );

        proposal.lock(lockedAt, configurationHash);

        Set<String> generatedReceiptHashes = new HashSet<>();
        List<EligibleVoter> eligibleVoters = eligibleVoterRepository.saveAll(
                eligibleStudentIndexes.stream()
                        .map(studentIndex -> new EligibleVoter(
                                proposal,
                                studentIndex,
                                generateUniqueReceiptHash(generatedReceiptHashes)
                        ))
                        .toList()
        );

        return toResponse(proposal, options, eligibleVoters);
    }

    private VotingProposal findProposal(UUID proposalId) {
        return votingProposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Voting proposal not found."));
    }

    private VotingProposal findProposalForUpdate(UUID proposalId) {
        return votingProposalRepository.findForUpdateById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Voting proposal not found."));
    }

    private void assertProposalExists(UUID proposalId) {
        if (!votingProposalRepository.existsById(proposalId)) {
            throw new ResourceNotFoundException("Voting proposal not found.");
        }
    }

    private static void assertDraft(VotingProposal proposal) {
        if (!proposal.isDraft()) {
            throw new BusinessRuleViolationException("Only draft voting proposals can be changed.");
        }
    }

    private void assertEditorAccess(VotingProposal proposal, String editorStudentIndex) {
        if (!identityAccess.hasActiveMandateInBody(editorStudentIndex, proposal.getCreatorBodyId(), now())) {
            throw new BusinessRuleViolationException("Only representatives of the same body can edit this proposal.");
        }
    }

    private static void validateProposalBeforeLock(VotingProposal proposal) {
        if (proposal.getBallotType() == null) {
            throw new BusinessRuleViolationException("Voting proposal ballot type is required before locking.");
        }

        if (proposal.getStartsAt() == null) {
            throw new BusinessRuleViolationException("Voting proposal start time is required before locking.");
        }

        if (proposal.getEndsAt() == null) {
            throw new BusinessRuleViolationException("Voting proposal end time is required before locking.");
        }

        if (!proposal.getEndsAt().isAfter(proposal.getStartsAt())) {
            throw new BusinessRuleViolationException("Voting proposal end time must be after start time.");
        }

        if (proposal.getQuorumType() == null) {
            throw new BusinessRuleViolationException("Voting proposal quorum type is required before locking.");
        }

        if (proposal.getDecisionRule() == null) {
            throw new BusinessRuleViolationException("Voting proposal decision rule is required before locking.");
        }

        normalizedQuorumValue(proposal.getQuorumType(), proposal.getQuorumValue());
    }

    private static Long normalizedQuorumValue(QuorumType quorumType, Long quorumValue) {
        if (quorumType == QuorumType.NONE) {
            if (quorumValue != null && quorumValue != 0) {
                throw new BusinessRuleViolationException("Quorum value must be empty or zero when quorum type is NONE.");
            }

            return null;
        }

        if (quorumValue == null || quorumValue < 1) {
            throw new BusinessRuleViolationException("Quorum value is required for this quorum type.");
        }

        if (quorumType == QuorumType.PERCENTAGE && quorumValue > 100) {
            throw new BusinessRuleViolationException("Percentage quorum value cannot exceed 100.");
        }

        return quorumValue;
    }

    private static String normalizedOptionalText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        return text.strip();
    }

    private static String normalizedRequiredText(String text, String errorMessage) {
        String normalizedText = Objects.requireNonNullElse(text, "").strip();

        if (normalizedText.isBlank()) {
            throw new BusinessRuleViolationException(errorMessage);
        }

        return normalizedText;
    }

    private static List<ProposalOption> normalizeOptions(List<CreateVotingOptionRequest> requests) {
        if (requests == null) {
            return List.of();
        }

        Set<Long> optionNumbers = new HashSet<>();
        Set<String> optionTexts = new HashSet<>();

        return requests.stream()
                .map(request -> {
                    if (request == null) {
                        throw new BusinessRuleViolationException("Voting option cannot be null.");
                    }

                    if (request.optionNumber() == null || request.optionNumber() < 1) {
                        throw new BusinessRuleViolationException("Voting option number must be positive.");
                    }

                    String text = Objects.requireNonNullElse(request.text(), "").strip();

                    if (text.isBlank()) {
                        throw new BusinessRuleViolationException("Voting option text is required.");
                    }

                    if (!optionNumbers.add(request.optionNumber())) {
                        throw new BusinessRuleViolationException("Voting option numbers must be unique within a proposal.");
                    }

                    if (!optionTexts.add(text)) {
                        throw new BusinessRuleViolationException("Voting option text must be unique within a proposal.");
                    }

                    return new ProposalOption(request.optionNumber(), text);
                })
                .sorted((left, right) -> left.optionNumber().compareTo(right.optionNumber()))
                .toList();
    }

    private static void assertNoOptionConflicts(List<VotingOption> existingOptions, List<ProposalOption> newOptions) {
        Set<Long> optionNumbers = new HashSet<>();
        Set<String> optionTexts = new HashSet<>();

        existingOptions.stream()
                .map(VotingProposalService::toProposalOption)
                .forEach(option -> {
                    optionNumbers.add(option.optionNumber());
                    optionTexts.add(option.text());
                });

        for (ProposalOption option : newOptions) {
            if (!optionNumbers.add(option.optionNumber())) {
                throw new BusinessRuleViolationException("Voting option number already exists for this proposal.");
            }

            if (!optionTexts.add(option.text())) {
                throw new BusinessRuleViolationException("Voting option text already exists for this proposal.");
            }
        }
    }

    private String generateUniqueReceiptHash(Set<String> generatedReceiptHashes) {
        for (int attempt = 0; attempt < MAX_RECEIPT_HASH_GENERATION_ATTEMPTS; attempt++) {
            String receiptHash = refreshTokenHasher.hash(refreshTokenGenerator.generate());

            if (generatedReceiptHashes.add(receiptHash) && !eligibleVoterRepository.existsByReceiptHash(receiptHash)) {
                return receiptHash;
            }
        }

        throw new IllegalStateException("Unable to generate unique eligible voter receipt hash.");
    }

    private String configurationHash(VotingProposal proposal, List<ProposalOption> options, List<String> eligibleVoters) {
        return configurationHash(
                proposal.getTitle(),
                proposal.getBallotType(),
                proposal.getStartsAt(),
                proposal.getEndsAt(),
                proposal.getQuorumType(),
                proposal.getQuorumValue(),
                proposal.getDecisionRule(),
                options,
                eligibleVoters
        );
    }

    private String configurationHash(
            String title,
            BallotType ballotType,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            QuorumType quorumType,
            Long quorumValue,
            DecisionRule decisionRule,
            List<ProposalOption> options,
            List<String> eligibleVoters
    ) {
        StringBuilder builder = new StringBuilder();

        appendHashField(builder, "title", title);
        appendHashField(builder, "ballot_type", ballotType.name());
        appendHashField(builder, "starts_at", startsAt.toInstant().toString());
        appendHashField(builder, "ends_at", endsAt.toInstant().toString());
        appendHashField(builder, "quorum_type", quorumType.name());
        appendHashField(builder, "quorum_value", quorumValue);
        appendHashField(builder, "decision_rule", decisionRule.name());

        options.stream()
                .sorted((left, right) -> left.optionNumber().compareTo(right.optionNumber()))
                .forEach(option -> appendHashField(
                        builder,
                        "option",
                        option.optionNumber() + ":" + option.text()
                ));

        eligibleVoters.stream()
                .sorted()
                .forEach(studentIndex -> appendHashField(
                        builder,
                        "eligible_voter",
                        studentIndex
                ));

        return refreshTokenHasher.hash(builder.toString());
    }

    private static void appendHashField(
            StringBuilder builder,
            String name,
            Object value
    ) {
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

    private OffsetDateTime now() {
        return OffsetDateTime.now(clock);
    }

    private static VotingProposalResponse toResponse(
            VotingProposal proposal,
            List<VotingOption> options,
            List<EligibleVoter> eligibleVoters
    ) {
        return new VotingProposalResponse(
                proposal.getVotingProposalId(),
                proposal.getCreatorStudentIndex(),
                proposal.getCreatorMandateId(),
                proposal.getCreatorBodyId(),
                proposal.getTitle(),
                proposal.getDescription(),
                proposal.getBallotType(),
                proposal.getStatus(),
                proposal.getStartsAt(),
                proposal.getEndsAt(),
                proposal.getQuorumType(),
                proposal.getQuorumValue(),
                proposal.getDecisionRule(),
                proposal.getConfigurationHash(),
                proposal.getCreatedAt(),
                proposal.getLockedAt(),
                options.stream()
                        .map(VotingProposalService::toResponse)
                        .toList(),
                eligibleVoters.stream()
                        .map(VotingProposalService::toResponse)
                        .toList()
        );
    }

    private static VotingOptionResponse toResponse(VotingOption option) {
        VotingOptionId votingOptionId = option.getVotingOptionId();

        return new VotingOptionResponse(
                votingOptionId.getVotingProposalId(),
                votingOptionId.getVotingOptionNumber(),
                option.getText()
        );
    }

    private static EligibleVoterResponse toResponse(EligibleVoter eligibleVoter) {
        EligibleVoterId eligibleVoterId = eligibleVoter.getEligibleVoterId();

        return new EligibleVoterResponse(
                eligibleVoterId.getVotingProposalId(),
                eligibleVoterId.getStudentIndex(),
                eligibleVoter.getVotedAt(),
                eligibleVoter.getReceiptHash()
        );
    }

    private static ProposalOption toProposalOption(VotingOption votingOption) {
        return new ProposalOption(
                votingOption.getVotingOptionNumber(),
                votingOption.getText()
        );
    }

    private record ProposalOption(Long optionNumber, String text) {
    }
}
