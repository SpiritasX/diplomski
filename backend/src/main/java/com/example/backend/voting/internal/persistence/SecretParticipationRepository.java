package com.example.backend.voting.internal.persistence;

import com.example.backend.voting.internal.domain.SecretParticipation;
import com.example.backend.voting.internal.domain.SecretParticipationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecretParticipationRepository extends JpaRepository<SecretParticipation, SecretParticipationId> {
}
