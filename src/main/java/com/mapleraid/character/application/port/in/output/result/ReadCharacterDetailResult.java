package com.mapleraid.character.application.port.in.output.result;

import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.type.EVerificationStatus;
import com.mapleraid.character.domain.type.EWorldGroup;
import com.mapleraid.core.dto.SelfValidating;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReadCharacterDetailResult extends SelfValidating<ReadCharacterDetailResult> {

    private final String id;

    private final String characterName;

    private final String worldName;

    private final EWorldGroup worldGroup;

    private final String characterClass;

    private final int characterLevel;

    private final String characterImageUrl;

    private final long combatPower;

    private final String equipmentJson;

    private final EVerificationStatus verificationStatus;

    private final LocalDateTime claimedAt;

    private final LocalDateTime verifiedAt;

    private final LocalDateTime lastSyncedAt;

    public ReadCharacterDetailResult(
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
        this.id = id;
        this.characterName = characterName;
        this.worldName = worldName;
        this.worldGroup = worldGroup;
        this.characterClass = characterClass;
        this.characterLevel = characterLevel;
        this.characterImageUrl = characterImageUrl;
        this.combatPower = combatPower;
        this.equipmentJson = equipmentJson;
        this.verificationStatus = verificationStatus;
        this.claimedAt = claimedAt;
        this.verifiedAt = verifiedAt;
        this.lastSyncedAt = lastSyncedAt;

        this.validateSelf();
    }

    public static ReadCharacterDetailResult of(
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
        return new ReadCharacterDetailResult(
                id,
                characterName,
                worldName,
                worldGroup,
                characterClass,
                characterLevel,
                characterImageUrl,
                combatPower,
                equipmentJson,
                verificationStatus,
                claimedAt,
                verifiedAt,
                lastSyncedAt
        );
    }

    public static ReadCharacterDetailResult from(Character character) {
        return new ReadCharacterDetailResult(
                character.getId().getValue().toString(),
                character.getCharacterName(),
                character.getWorldName(),
                character.getEWorldGroup(),
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
