package com.mapleraid.character.application.port.out;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.character.domain.VerificationChallenge;
import com.mapleraid.character.domain.VerificationChallengeId;
import com.mapleraid.character.domain.type.EChallengeStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VerificationChallengeRepository {

    VerificationChallenge save(VerificationChallenge challenge);

    Optional<VerificationChallenge> findById(VerificationChallengeId id);

    Optional<VerificationChallenge> findByCharacterIdAndStatus(CharacterId characterId, EChallengeStatus status);

    List<VerificationChallenge> findPendingByCharacterId(CharacterId characterId);

    List<VerificationChallenge> findExpiredPendingChallenges(LocalDateTime before);

    int countByCharacterIdAndCreatedAtAfter(CharacterId characterId, LocalDateTime after);

    Optional<LocalDateTime> findLastFailedAtByCharacterId(CharacterId characterId);
}
