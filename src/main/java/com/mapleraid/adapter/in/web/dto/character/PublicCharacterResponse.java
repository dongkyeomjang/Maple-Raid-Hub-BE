package com.mapleraid.adapter.in.web.dto.character;

import com.mapleraid.domain.character.Character;
import com.mapleraid.domain.character.VerificationStatus;
import com.mapleraid.domain.character.WorldGroup;

import java.time.Instant;

/**
 * 공개 캐릭터 정보 (다른 유저가 볼 수 있는 정보)
 */
public record PublicCharacterResponse(
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
        Instant lastSyncedAt
) {
    public static PublicCharacterResponse from(Character character) {
        return new PublicCharacterResponse(
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
                character.getLastSyncedAt()
        );
    }
}
