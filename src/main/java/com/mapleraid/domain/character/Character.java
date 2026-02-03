package com.mapleraid.domain.character;

import com.mapleraid.domain.common.DomainException;
import com.mapleraid.domain.user.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Character Aggregate Root
 * 메이플스토리 캐릭터 및 소유권 정보
 */
public class Character {
    private final CharacterId id;
    private final String characterName;
    private final String worldName;
    private final WorldGroup worldGroup;
    private String characterClass;
    private int characterLevel;
    private String characterImageUrl;
    private String ocid; // Nexon Open API 캐릭터 식별자

    // Combat stats
    private long combatPower;
    private String equipmentJson; // JSON string of equipment info

    // Ownership
    private UserId ownerId;
    private VerificationStatus verificationStatus;
    private Instant verifiedAt;
    private Instant claimedAt;

    // Sync
    private Instant lastSyncedAt;
    private Instant updatedAt;

    private Character(CharacterId id, String characterName, String worldName,
                      WorldGroup worldGroup, UserId ownerId) {
        this.id = Objects.requireNonNull(id);
        this.characterName = Objects.requireNonNull(characterName);
        this.worldName = Objects.requireNonNull(worldName);
        this.worldGroup = Objects.requireNonNull(worldGroup);
        this.ownerId = Objects.requireNonNull(ownerId);
        this.verificationStatus = VerificationStatus.UNVERIFIED_CLAIMED;
        this.claimedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 캐릭터 클레임 (등록)
     */
    public static Character claim(CharacterId id, String characterName, String worldName,
                                  WorldGroup worldGroup, UserId ownerId, String ocid) {
        Character character = new Character(id, characterName, worldName, worldGroup, ownerId);
        character.ocid = ocid;
        return character;
    }

    /**
     * 복원용 팩토리 (영속성 어댑터에서 사용)
     */
    public static Character reconstitute(
            CharacterId id, String characterName, String worldName, WorldGroup worldGroup,
            String characterClass, int characterLevel, String characterImageUrl, String ocid,
            long combatPower, String equipmentJson,
            UserId ownerId, VerificationStatus verificationStatus,
            Instant verifiedAt, Instant claimedAt, Instant lastSyncedAt, Instant updatedAt) {
        Character character = new Character(id, characterName, worldName, worldGroup, ownerId);
        character.characterClass = characterClass;
        character.characterLevel = characterLevel;
        character.characterImageUrl = characterImageUrl;
        character.ocid = ocid;
        character.combatPower = combatPower;
        character.equipmentJson = equipmentJson;
        character.verificationStatus = verificationStatus;
        character.verifiedAt = verifiedAt;
        character.claimedAt = claimedAt;
        character.lastSyncedAt = lastSyncedAt;
        character.updatedAt = updatedAt;
        return character;
    }

    /**
     * 소유권 인증 완료
     */
    public void markAsVerified() {
        if (this.verificationStatus == VerificationStatus.REVOKED) {
            throw new DomainException("CHARACTER_ALREADY_REVOKED",
                    "이 캐릭터의 소유권이 이미 다른 사용자에게 이전되었습니다.");
        }
        this.verificationStatus = VerificationStatus.VERIFIED_OWNER;
        this.verifiedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 다른 사용자가 인증 성공 시 기존 클레임 무효화
     */
    public void revoke() {
        this.verificationStatus = VerificationStatus.REVOKED;
        this.updatedAt = Instant.now();
    }

    /**
     * 분쟁 상태로 전환 (다른 사용자가 인증 시도 중)
     */
    public void markAsDisputed() {
        if (this.verificationStatus == VerificationStatus.VERIFIED_OWNER) {
            this.verificationStatus = VerificationStatus.DISPUTED;
            this.updatedAt = Instant.now();
        }
    }

    /**
     * Nexon API에서 가져온 정보로 동기화
     */
    public void syncFromNexonApi(String characterClass, int characterLevel,
                                 String characterImageUrl, String ocid) {
        this.characterClass = characterClass;
        this.characterLevel = characterLevel;
        this.characterImageUrl = characterImageUrl;
        this.ocid = ocid;
        this.lastSyncedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Nexon API에서 가져온 전체 정보로 동기화 (전투력, 장비 포함)
     */
    public void syncFromNexonApi(String characterClass, int characterLevel,
                                 String characterImageUrl, String ocid,
                                 long combatPower, String equipmentJson) {
        this.characterClass = characterClass;
        this.characterLevel = characterLevel;
        this.characterImageUrl = characterImageUrl;
        this.ocid = ocid;
        this.combatPower = combatPower;
        this.equipmentJson = equipmentJson;
        this.lastSyncedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 인증된 상태인지 확인
     */
    public boolean isVerified() {
        return verificationStatus == VerificationStatus.VERIFIED_OWNER;
    }

    /**
     * 인증 가능한 상태인지 확인
     */
    public boolean canStartVerification() {
        return verificationStatus == VerificationStatus.UNVERIFIED_CLAIMED
                || verificationStatus == VerificationStatus.DISPUTED;
    }

    /**
     * 파티 활동 가능한 상태인지 확인
     */
    public boolean canParticipateInParty() {
        return verificationStatus == VerificationStatus.VERIFIED_OWNER;
    }

    // Getters
    public CharacterId getId() {
        return id;
    }

    public String getCharacterName() {
        return characterName;
    }

    public String getWorldName() {
        return worldName;
    }

    public WorldGroup getWorldGroup() {
        return worldGroup;
    }

    public String getCharacterClass() {
        return characterClass;
    }

    public int getCharacterLevel() {
        return characterLevel;
    }

    public String getCharacterImageUrl() {
        return characterImageUrl;
    }

    public String getOcid() {
        return ocid;
    }

    public long getCombatPower() {
        return combatPower;
    }

    public String getEquipmentJson() {
        return equipmentJson;
    }

    public UserId getOwnerId() {
        return ownerId;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public Instant getClaimedAt() {
        return claimedAt;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
