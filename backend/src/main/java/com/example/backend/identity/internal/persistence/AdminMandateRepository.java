package com.example.backend.identity.internal.persistence;

import com.example.backend.identity.internal.domain.AdminMandate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AdminMandateRepository extends JpaRepository<AdminMandate, UUID> {
}
