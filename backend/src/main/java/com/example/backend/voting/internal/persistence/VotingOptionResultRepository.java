package com.example.backend.voting.internal.persistence;

import com.example.backend.voting.internal.domain.VotingOptionResult;
import com.example.backend.voting.internal.domain.VotingOptionResultId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VotingOptionResultRepository extends JpaRepository<VotingOptionResult, VotingOptionResultId> {
}
