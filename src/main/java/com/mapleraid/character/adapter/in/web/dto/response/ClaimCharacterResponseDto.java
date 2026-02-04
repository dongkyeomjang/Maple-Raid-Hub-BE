package com.mapleraid.character.adapter.in.web.dto.response;

import com.mapleraid.character.application.port.in.output.result.ClaimCharacterResult;
import com.mapleraid.character.domain.type.EVerificationStatus;
import com.mapleraid.character.domain.type.EWorldGroup;

import java.time.LocalDateTime;

public record ClaimCharacterResponseDto(
        String id,
        String characterName,
        String worldName,
        EWorldGroup worldGroup,
        String characterClass,
        int characterLevel,
        String characterImageUrl,
        long combatPower,
        String equipmentJson,
        EVerificationStatus verificationStatus,
        LocalDateTime claimedAt,
        LocalDateTime verifiedAt,
        LocalDateTime lastSyncedAt
) {
    public static ClaimCharacterResponseDto from(ClaimCharacterResult character) {
        return new ClaimCharacterResponseDto(
                character.getId(),
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
