package com.mapleraid.adapter.out.persistence;

import com.mapleraid.adapter.out.persistence.entity.VerificationChallengeJpaEntity;
import com.mapleraid.adapter.out.persistence.repository.VerificationChallengeJpaRepository;
import com.mapleraid.application.port.out.VerificationChallengeRepository;
import com.mapleraid.domain.character.ChallengeStatus;
import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.character.VerificationChallenge;
import com.mapleraid.domain.character.VerificationChallengeId;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class VerificationChallengeRepositoryAdapter implements VerificationChallengeRepository {

    private final VerificationChallengeJpaRepository jpaRepository;

    public VerificationChallengeRepositoryAdapter(VerificationChallengeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public VerificationChallenge save(VerificationChallenge challenge) {
        VerificationChallengeJpaEntity entity = VerificationChallengeJpaEntity.fromDomain(challenge);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<VerificationChallenge> findById(VerificationChallengeId id) {
        return jpaRepository.findById(id.getValue().toString())
                .map(VerificationChallengeJpaEntity::toDomain);
    }

    @Override
    public Optional<VerificationChallenge> findByCharacterIdAndStatus(CharacterId characterId, ChallengeStatus status) {
        return jpaRepository.findByCharacterIdAndStatus(characterId.getValue().toString(), status)
                .map(VerificationChallengeJpaEntity::toDomain);
    }

    @Override
    public int countByCharacterIdAndCreatedAtAfter(CharacterId characterId, Instant after) {
        return jpaRepository.countByCharacterIdAndCreatedAtAfter(characterId.getValue().toString(), after);
    }

    @Override
    public Optional<Instant> findLastFailedAtByCharacterId(CharacterId characterId) {
        return jpaRepository.findLastFailedAtByCharacterId(characterId.getValue().toString());
    }

    @Override
    public List<VerificationChallenge> findExpiredPendingChallenges(Instant now) {
        return jpaRepository.findExpiredPendingChallenges(now).stream()
                .map(VerificationChallengeJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<VerificationChallenge> findPendingByCharacterId(CharacterId characterId) {
        return jpaRepository.findAllByCharacterIdAndStatus(characterId.getValue().toString(), ChallengeStatus.PENDING)
                .stream()
                .map(VerificationChallengeJpaEntity::toDomain)
                .toList();
    }
}
