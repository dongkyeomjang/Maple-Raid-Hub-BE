package com.mapleraid.character.adapter.out.persistence.jpa;

import com.mapleraid.character.domain.type.EChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VerificationChallengeJpaRepository extends JpaRepository<VerificationChallengeJpaEntity, String> {

    Optional<VerificationChallengeJpaEntity> findByCharacterIdAndStatus(String characterId, EChallengeStatus status);

    List<VerificationChallengeJpaEntity> findAllByCharacterIdAndStatus(String characterId, EChallengeStatus status);

    int countByCharacterIdAndCreatedAtAfter(String characterId, LocalDateTime after);

    @Query("SELECT MAX(c.createdAt) FROM VerificationChallengeJpaEntity c " +
            "WHERE c.characterId = :characterId AND c.status = 'FAILED'")
    Optional<LocalDateTime> findLastFailedAtByCharacterId(@Param("characterId") String characterId);

    @Query("SELECT c FROM VerificationChallengeJpaEntity c " +
            "WHERE c.status = 'PENDING' AND c.expiresAt < :now")
    List<VerificationChallengeJpaEntity> findExpiredPendingChallenges(@Param("now") LocalDateTime now);
}
