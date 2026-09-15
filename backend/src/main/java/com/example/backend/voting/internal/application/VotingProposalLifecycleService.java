package com.example.backend.voting.internal.application;

import com.example.backend.voting.internal.domain.VotingProposal;
import com.example.backend.voting.internal.domain.enums.VotingProposalStatus;
import com.example.backend.voting.internal.persistence.VotingProposalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class VotingProposalLifecycleService {

    private final VotingProposalRepository votingProposalRepository;

    public VotingProposalLifecycleService(VotingProposalRepository votingProposalRepository) {
        this.votingProposalRepository = votingProposalRepository;
    }

    @Transactional
    public void updateDueProposals(OffsetDateTime now) {
        List<VotingProposal> proposals = votingProposalRepository.findDueForLifecycleUpdate(
                VotingProposalStatus.LOCKED,
                VotingProposalStatus.OPEN,
                now
        );

        for (VotingProposal proposal : proposals) {
            updateProposal(now, proposal);
        }
    }

    // TODO: publish proposal closed event
    public void updateProposal(OffsetDateTime now, VotingProposal proposal) {
        if (proposal.isLocked()) {
            if (!now.isBefore(proposal.getEndsAt())) {
                proposal.close();
            } else if (!now.isBefore(proposal.getStartsAt())) {
                proposal.open();
            }
        } else if (proposal.isOpen() && !now.isBefore(proposal.getEndsAt())) {
            proposal.close();
        }
    }
}
