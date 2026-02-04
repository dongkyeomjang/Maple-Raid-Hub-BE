package com.mapleraid.character.adapter.in.web.dto.response;

import com.mapleraid.character.application.port.in.output.result.ReadMyCharactersResult;
import com.mapleraid.character.domain.type.EVerificationStatus;
import com.mapleraid.character.domain.type.EWorldGroup;

import java.time.LocalDateTime;
import java.util.List;

public record ReadMyCharactersResponseDto(
        List<CharacterInfo> characters
) {
    public static ReadMyCharactersResponseDto from(ReadMyCharactersResult result) {
        List<CharacterInfo> characterInfos = result.getCharacters().stream()
                .map(CharacterInfo::from)
                .toList();
        return new ReadMyCharactersResponseDto(characterInfos);
    }

    public record CharacterInfo(
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
        public static CharacterInfo from(ReadMyCharactersResult.CharacterInfo info) {
            return new CharacterInfo(
                    info.getId(),
                    info.getCharacterName(),
                    info.getWorldName(),
                    info.getWorldGroup(),
                    info.getCharacterClass(),
                    info.getCharacterLevel(),
                    info.getCharacterImageUrl(),
                    info.getCombatPower(),
                    info.getEquipmentJson(),
                    info.getVerificationStatus(),
                    info.getClaimedAt(),
                    info.getVerifiedAt(),
                    info.getLastSyncedAt()
            );
        }
    }
}
