package com.mapleraid.adapter.out.persistence.entity;

import com.mapleraid.domain.character.Character;
import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.character.VerificationStatus;
import com.mapleraid.domain.character.WorldGroup;
import com.mapleraid.domain.user.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
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
    private WorldGroup worldGroup;

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
    private VerificationStatus verificationStatus;

    @Column(name = "claimed_at", nullable = false)
    private Instant claimedAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    protected CharacterJpaEntity() {
    }

    public static CharacterJpaEntity fromDomain(Character character) {
        CharacterJpaEntity entity = new CharacterJpaEntity();
        entity.id = character.getId().getValue().toString();
        entity.characterName = character.getCharacterName();
        entity.worldName = character.getWorldName();
        entity.worldGroup = character.getWorldGroup();
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
                worldGroup,
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

    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public String getCharacterName() {
        return characterName;
    }

    public String getWorldName() {
        return worldName;
    }
}
