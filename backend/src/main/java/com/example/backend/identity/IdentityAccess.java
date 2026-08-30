package com.example.backend.identity;

import com.example.backend.identity.internal.persistence.AccountRepository;
import com.example.backend.identity.internal.persistence.RepresentativeBodyRepository;
import com.example.backend.identity.internal.persistence.RepresentativeMandateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class IdentityAccess {

    private final AccountRepository accountRepository;
    private final RepresentativeBodyRepository representativeBodyRepository;
    private final RepresentativeMandateRepository representativeMandateRepository;

    public IdentityAccess(
            AccountRepository accountRepository,
            RepresentativeBodyRepository representativeBodyRepository,
            RepresentativeMandateRepository representativeMandateRepository
    ) {
        this.accountRepository = accountRepository;
        this.representativeBodyRepository = representativeBodyRepository;
        this.representativeMandateRepository = representativeMandateRepository;
    }

    @Transactional(readOnly = true)
    public boolean representativeBodyExists(UUID representativeBodyId) {
        return representativeBodyRepository.existsById(representativeBodyId);
    }

    @Transactional(readOnly = true)
    public List<String> findEligibleVoterStudentIndexes(OffsetDateTime now) {
        return accountRepository.findActiveStudentIndexesWithoutActiveMandate(now);
    }

    public record ActiveMandate(UUID mandateId, UUID bodyId) {
    }

    @Transactional(readOnly = true)
    public Optional<ActiveMandate> findActiveMandate(String studentIndex, OffsetDateTime now) {
        return representativeMandateRepository.findActiveMandatesForStudent(studentIndex, now)
                .stream()
                .findFirst()
                .map(m -> new ActiveMandate(m.getMandateId(), m.getBody().getBodyId()));
    }

    @Transactional(readOnly = true)
    public boolean hasActiveMandateInBody(String studentIndex, UUID bodyId, OffsetDateTime now) {
        return representativeMandateRepository.existsActiveMandateForStudentAndBody(studentIndex, bodyId, now);
    }
}
