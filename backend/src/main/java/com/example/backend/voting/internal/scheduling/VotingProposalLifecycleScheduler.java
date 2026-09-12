package com.example.backend.voting.internal.scheduling;

import com.example.backend.voting.internal.application.VotingProposalLifecycleService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.OffsetDateTime;

@Component
public class VotingProposalLifecycleScheduler {

    private final VotingProposalLifecycleService lifecycleService;
    private final Clock clock;

    public VotingProposalLifecycleScheduler(
            VotingProposalLifecycleService lifecycleService,
            Clock clock
    ) {
        this.lifecycleService = lifecycleService;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${voting.lifecycle.poll-ms:5000}")
    public void updateProposalStatuses() {
        lifecycleService.updateDueProposals(
                OffsetDateTime.now(clock)
        );
    }
}
