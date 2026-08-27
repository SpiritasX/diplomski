package com.example.backend.identity.internal.persistence;

import com.example.backend.identity.internal.domain.RefreshSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RefreshSessionRepository extends JpaRepository<RefreshSession, UUID> {
}
