package com.mapleraid.character.adapter.in.web.dto.response;

import com.mapleraid.character.application.port.in.output.result.ReadCharacterDetailResult;
import com.mapleraid.character.domain.type.EVerificationStatus;
import com.mapleraid.character.domain.type.EWorldGroup;

import java.time.LocalDateTime;

public record ReadCharacterDetailResponseDto(
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
    public static ReadCharacterDetailResponseDto from(ReadCharacterDetailResult result) {
        return new ReadCharacterDetailResponseDto(
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
