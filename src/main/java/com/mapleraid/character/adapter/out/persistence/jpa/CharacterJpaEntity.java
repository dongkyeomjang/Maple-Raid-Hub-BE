package com.mapleraid.character.adapter.out.persistence.jpa;

import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.character.domain.type.EVerificationStatus;
import com.mapleraid.character.domain.type.EWorldGroup;
import com.mapleraid.user.domain.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "characters")
public class CharacterJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "character_name", nullable = false, length = 50)
    private String characterName;

    @Column(name = "world_name", nullable = false, length = 30)
    private String worldName;

    @Enumerated(EnumType.STRING)
    @Column(name = "world_group", nullable = false, length = 20)
    private EWorldGroup EWorldGroup;

    @Column(name = "owner_id", nullable = false, length = 36)
    private String ownerId;

    @Column(name = "ocid", length = 100)
    private String ocid;

    @Column(name = "character_class", length = 50)
    private String characterClass;

    @Column(name = "character_level")
    private int characterLevel;

    @Column(name = "character_image_url", length = 2048)
    private String characterImageUrl;

    @Column(name = "combat_power")
    private Long combatPower;

    @Column(name = "equipment_json", columnDefinition = "TEXT")
    private String equipmentJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    private EVerificationStatus verificationStatus;

    @Column(name = "claimed_at", nullable = false)
    private LocalDateTime claimedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    public static CharacterJpaEntity fromDomain(Character character) {
        CharacterJpaEntity entity = new CharacterJpaEntity();
        entity.id = character.getId().getValue().toString();
        entity.characterName = character.getCharacterName();
        entity.worldName = character.getWorldName();
        entity.EWorldGroup = character.getEWorldGroup();
        entity.ownerId = character.getOwnerId().getValue().toString();
        entity.ocid = character.getOcid();
        entity.characterClass = character.getCharacterClass();
        entity.characterLevel = character.getCharacterLevel();
        entity.characterImageUrl = character.getCharacterImageUrl();
        entity.combatPower = character.getCombatPower();
        entity.equipmentJson = character.getEquipmentJson();
        entity.verificationStatus = character.getVerificationStatus();
        entity.claimedAt = character.getClaimedAt();
        entity.updatedAt = character.getUpdatedAt();
        entity.verifiedAt = character.getVerifiedAt();
        entity.lastSyncedAt = character.getLastSyncedAt();
        return entity;
    }

    public Character toDomain() {
        return Character.reconstitute(
                CharacterId.of(id),
                characterName,
                worldName,
                EWorldGroup,
                characterClass,
                characterLevel,
                characterImageUrl,
                ocid,
                combatPower != null ? combatPower : 0L,
                equipmentJson,
                UserId.of(ownerId),
                verificationStatus,
                verifiedAt,
                claimedAt,
                lastSyncedAt,
                updatedAt
        );
    }
}
