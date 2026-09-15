package com.example.backend.voting.internal.application;

import com.example.backend.voting.internal.domain.VotingProposal;
import com.example.backend.voting.internal.domain.enums.VotingProposalStatus;
import com.example.backend.voting.internal.persistence.VotingProposalRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class VotingProposalLifecycleService {

    private final VotingProposalRepository votingProposalRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VotingProposalLifecycleService(
            VotingProposalRepository votingProposalRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.votingProposalRepository = votingProposalRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void updateDueProposals(OffsetDateTime now) {
        List<VotingProposal> proposals = votingProposalRepository.findDueForLifecycleUpdate(
                VotingProposalStatus.LOCKED,
                VotingProposalStatus.OPEN,
                now
        );

        for (VotingProposal proposal : proposals) {
            LifecycleTransition transition = updateProposal(now, proposal);

            if (transition == LifecycleTransition.CLOSED) {
                eventPublisher.publishEvent(
                        new VotingProposalClosedEvent(
                                proposal.getVotingProposalId(),
                                now
                        )
                );
            }
        }
    }

    public LifecycleTransition updateProposal(OffsetDateTime now, VotingProposal proposal) {
        if (proposal.isLocked()) {
            if (!now.isBefore(proposal.getEndsAt())) {
                proposal.close();
                return LifecycleTransition.CLOSED;
            } else if (!now.isBefore(proposal.getStartsAt())) {
                proposal.open();
                return LifecycleTransition.OPENED;
            }
        } else if (proposal.isOpen() && !now.isBefore(proposal.getEndsAt())) {
            proposal.close();
            return LifecycleTransition.CLOSED;
        }
        return LifecycleTransition.NONE;
    }

    public enum LifecycleTransition {
        NONE,
        OPENED,
        CLOSED
    }
}
