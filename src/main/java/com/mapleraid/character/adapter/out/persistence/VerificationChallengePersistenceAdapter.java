package com.mapleraid.character.adapter.out.persistence;

import com.mapleraid.character.adapter.out.persistence.jpa.VerificationChallengeJpaEntity;
import com.mapleraid.character.adapter.out.persistence.jpa.VerificationChallengeJpaRepository;
import com.mapleraid.character.application.port.out.VerificationChallengeRepository;
import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.character.domain.VerificationChallenge;
import com.mapleraid.character.domain.VerificationChallengeId;
import com.mapleraid.character.domain.type.EChallengeStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class VerificationChallengePersistenceAdapter implements VerificationChallengeRepository {

    private final VerificationChallengeJpaRepository jpaRepository;

    public VerificationChallengePersistenceAdapter(VerificationChallengeJpaRepository jpaRepository) {
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
    public Optional<VerificationChallenge> findByCharacterIdAndStatus(CharacterId characterId, EChallengeStatus status) {
        return jpaRepository.findByCharacterIdAndStatus(characterId.getValue().toString(), status)
                .map(VerificationChallengeJpaEntity::toDomain);
    }

    @Override
    public int countByCharacterIdAndCreatedAtAfter(CharacterId characterId, LocalDateTime after) {
        return jpaRepository.countByCharacterIdAndCreatedAtAfter(characterId.getValue().toString(), after);
    }

    @Override
    public Optional<LocalDateTime> findLastFailedAtByCharacterId(CharacterId characterId) {
        return jpaRepository.findLastFailedAtByCharacterId(characterId.getValue().toString());
    }

    @Override
    public List<VerificationChallenge> findExpiredPendingChallenges(LocalDateTime now) {
        return jpaRepository.findExpiredPendingChallenges(now).stream()
                .map(VerificationChallengeJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<VerificationChallenge> findPendingByCharacterId(CharacterId characterId) {
        return jpaRepository.findAllByCharacterIdAndStatus(characterId.getValue().toString(), EChallengeStatus.PENDING)
                .stream()
                .map(VerificationChallengeJpaEntity::toDomain)
                .toList();
    }
}
