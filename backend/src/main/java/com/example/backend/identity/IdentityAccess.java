package com.example.backend.identity;

import com.example.backend.identity.internal.persistence.AccountRepository;
import com.example.backend.identity.internal.persistence.RepresentativeBodyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class IdentityAccess {

    private final AccountRepository accountRepository;
    private final RepresentativeBodyRepository representativeBodyRepository;

    public IdentityAccess(
            AccountRepository accountRepository,
            RepresentativeBodyRepository representativeBodyRepository
    ) {
        this.accountRepository = accountRepository;
        this.representativeBodyRepository = representativeBodyRepository;
    }

    @Transactional(readOnly = true)
    public boolean representativeBodyExists(UUID representativeBodyId) {
        return representativeBodyRepository.existsById(representativeBodyId);
    }

    @Transactional(readOnly = true)
    public List<String> findEligibleVoterStudentIndexes(OffsetDateTime now) {
        return accountRepository.findActiveStudentIndexesWithoutActiveMandate(now);
    }
}
