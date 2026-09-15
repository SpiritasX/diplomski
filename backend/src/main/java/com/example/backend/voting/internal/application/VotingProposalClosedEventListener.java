package com.example.backend.voting.internal.application;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class VotingProposalClosedEventListener {

    private final VotingResultService votingResultService;

    public VotingProposalClosedEventListener(VotingResultService votingResultService) {
        this.votingResultService = votingResultService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void computeResult(VotingProposalClosedEvent event) {
        votingResultService.computeAndStoreResult(
                event.proposalId(),
                event.closedAt()
        );
    }
}
