package com.example.backend.voting.internal.persistence;

import com.example.backend.voting.internal.domain.SecretBallot;
import com.example.backend.voting.internal.domain.SecretBallotId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecretBallotRepository extends JpaRepository<SecretBallot, SecretBallotId> {
}
