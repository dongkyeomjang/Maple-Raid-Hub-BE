package com.mapleraid.character.adapter.in.web.dto.response;

import com.mapleraid.character.application.port.in.output.result.SyncCharacterResult;
import com.mapleraid.character.domain.type.EVerificationStatus;
import com.mapleraid.character.domain.type.EWorldGroup;

import java.time.LocalDateTime;

public record SyncCharacterResponseDto(
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
    public static SyncCharacterResponseDto from(SyncCharacterResult result) {
        return new SyncCharacterResponseDto(
                result.getId(),
                result.getCharacterName(),
                result.getWorldName(),
                result.getWorldGroup(),
                result.getCharacterClass(),
                result.getCharacterLevel(),
                result.getCharacterImageUrl(),
                result.getCombatPower(),
                result.getEquipmentJson(),
                result.getVerificationStatus(),
                result.getClaimedAt(),
                result.getVerifiedAt(),
                result.getLastSyncedAt()
        );
    }
}
