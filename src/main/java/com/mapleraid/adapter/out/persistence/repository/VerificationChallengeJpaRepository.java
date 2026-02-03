package com.mapleraid.adapter.out.persistence.repository;

import com.mapleraid.adapter.out.persistence.entity.VerificationChallengeJpaEntity;
import com.mapleraid.domain.character.ChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface VerificationChallengeJpaRepository extends JpaRepository<VerificationChallengeJpaEntity, String> {

    Optional<VerificationChallengeJpaEntity> findByCharacterIdAndStatus(String characterId, ChallengeStatus status);

    List<VerificationChallengeJpaEntity> findAllByCharacterIdAndStatus(String characterId, ChallengeStatus status);

    int countByCharacterIdAndCreatedAtAfter(String characterId, Instant after);

    @Query("SELECT MAX(c.createdAt) FROM VerificationChallengeJpaEntity c " +
            "WHERE c.characterId = :characterId AND c.status = 'FAILED'")
    Optional<Instant> findLastFailedAtByCharacterId(@Param("characterId") String characterId);

    @Query("SELECT c FROM VerificationChallengeJpaEntity c " +
            "WHERE c.status = 'PENDING' AND c.expiresAt < :now")
    List<VerificationChallengeJpaEntity> findExpiredPendingChallenges(@Param("now") Instant now);
}
