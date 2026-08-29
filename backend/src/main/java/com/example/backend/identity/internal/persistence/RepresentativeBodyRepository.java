package com.example.backend.identity.internal.persistence;

import com.example.backend.identity.internal.domain.RepresentativeBody;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RepresentativeBodyRepository extends JpaRepository<RepresentativeBody, UUID> {
}
