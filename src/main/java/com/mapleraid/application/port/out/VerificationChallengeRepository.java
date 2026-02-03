package com.mapleraid.application.port.out;

import com.mapleraid.domain.character.ChallengeStatus;
import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.character.VerificationChallenge;
import com.mapleraid.domain.character.VerificationChallengeId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface VerificationChallengeRepository {

    VerificationChallenge save(VerificationChallenge challenge);

    Optional<VerificationChallenge> findById(VerificationChallengeId id);

    Optional<VerificationChallenge> findByCharacterIdAndStatus(CharacterId characterId, ChallengeStatus status);

    List<VerificationChallenge> findPendingByCharacterId(CharacterId characterId);

    List<VerificationChallenge> findExpiredPendingChallenges(Instant before);

    int countByCharacterIdAndCreatedAtAfter(CharacterId characterId, Instant after);

    Optional<Instant> findLastFailedAtByCharacterId(CharacterId characterId);
}
