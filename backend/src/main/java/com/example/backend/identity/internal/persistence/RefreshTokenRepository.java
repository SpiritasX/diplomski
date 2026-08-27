package com.example.backend.identity.internal.persistence;

import com.example.backend.identity.internal.domain.RefreshSession;
import com.example.backend.identity.internal.domain.RefreshToken;
import com.example.backend.identity.internal.domain.RefreshTokenStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select token
            from RefreshToken token
            join fetch token.session
            where token.tokenHash = :tokenHash
            """)
    Optional<RefreshToken> findForUpdateByTokenHash(
            @Param("tokenHash") String tokenHash
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select token
            from RefreshToken token
            where token.session = :session
                and token.status = :status
            """)
    List<RefreshToken> findBySessionAndStatusForUpdate(
            @Param("session") RefreshSession session,
            @Param("status") RefreshTokenStatus status
    );
}
