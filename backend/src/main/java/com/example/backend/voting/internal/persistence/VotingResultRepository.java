package com.example.backend.voting.internal.persistence;

import com.example.backend.voting.internal.domain.VotingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VotingResultRepository extends JpaRepository<VotingResult, UUID> {
}
