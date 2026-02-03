package com.mapleraid.adapter.in.web.dto.character;

import com.mapleraid.domain.character.Character;
import com.mapleraid.domain.character.VerificationStatus;
import com.mapleraid.domain.character.WorldGroup;

import java.time.Instant;

public record CharacterResponse(
        String id,
        String characterName,
        String worldName,
        WorldGroup worldGroup,
        String characterClass,
        int characterLevel,
        String characterImageUrl,
        long combatPower,
        String equipmentJson,
        VerificationStatus verificationStatus,
        Instant claimedAt,
        Instant verifiedAt,
        Instant lastSyncedAt
) {
    public static CharacterResponse from(Character character) {
        return new CharacterResponse(
                character.getId().getValue().toString(),
                character.getCharacterName(),
                character.getWorldName(),
                character.getWorldGroup(),
                character.getCharacterClass(),
                character.getCharacterLevel(),
                character.getCharacterImageUrl(),
                character.getCombatPower(),
                character.getEquipmentJson(),
                character.getVerificationStatus(),
                character.getClaimedAt(),
                character.getVerifiedAt(),
                character.getLastSyncedAt()
        );
    }
}
