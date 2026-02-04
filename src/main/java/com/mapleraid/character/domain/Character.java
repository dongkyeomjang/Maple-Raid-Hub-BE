package com.mapleraid.character.domain;

import com.mapleraid.character.domain.type.EVerificationStatus;
import com.mapleraid.character.domain.type.EWorldGroup;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Character Aggregate Root
 * 메이플스토리 캐릭터 및 소유권 정보
 */
@Getter
public class Character {
    private final CharacterId id;
    private final String characterName;
    private final String worldName;
    private final EWorldGroup EWorldGroup;
    // Ownership
    private final UserId ownerId;
    private String characterClass;
    private int characterLevel;
    private String characterImageUrl;
    private String ocid; // Nexon Open API 캐릭터 식별자
    // Combat stats
    private long combatPower;
    private String equipmentJson; // JSON string of equipment info
    private EVerificationStatus verificationStatus;
    private LocalDateTime verifiedAt;
    private LocalDateTime claimedAt;

    // Sync
    private LocalDateTime lastSyncedAt;
    private LocalDateTime updatedAt;

    private Character(CharacterId id, String characterName, String worldName,
                      EWorldGroup EWorldGroup, UserId ownerId) {
        this.id = Objects.requireNonNull(id);
        this.characterName = Objects.requireNonNull(characterName);
        this.worldName = Objects.requireNonNull(worldName);
        this.EWorldGroup = Objects.requireNonNull(EWorldGroup);
        this.ownerId = Objects.requireNonNull(ownerId);
        this.verificationStatus = EVerificationStatus.UNVERIFIED_CLAIMED;
        this.claimedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 캐릭터 클레임 (등록)
     */
    public static Character claim(CharacterId id, String characterName, String worldName,
                                  EWorldGroup EWorldGroup, UserId ownerId, String ocid) {
        Character character = new Character(id, characterName, worldName, EWorldGroup, ownerId);
        character.ocid = ocid;
        return character;
    }

    /**
     * 복원용 팩토리 (영속성 어댑터에서 사용)
     */
    public static Character reconstitute(
            CharacterId id, String characterName, String worldName, EWorldGroup EWorldGroup,
            String characterClass, int characterLevel, String characterImageUrl, String ocid,
            long combatPower, String equipmentJson,
            UserId ownerId, EVerificationStatus verificationStatus,
            LocalDateTime verifiedAt, LocalDateTime claimedAt, LocalDateTime lastSyncedAt, LocalDateTime updatedAt) {
        Character character = new Character(id, characterName, worldName, EWorldGroup, ownerId);
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
        if (this.verificationStatus == EVerificationStatus.REVOKED) {
            throw new CommonException(ErrorCode.CHARACTER_ALREADY_REVOKED);
        }
        this.verificationStatus = EVerificationStatus.VERIFIED_OWNER;
        this.verifiedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 다른 사용자가 인증 성공 시 기존 클레임 무효화
     */
    public void revoke() {
        this.verificationStatus = EVerificationStatus.REVOKED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 분쟁 상태로 전환 (다른 사용자가 인증 시도 중)
     */
    public void markAsDisputed() {
        if (this.verificationStatus == EVerificationStatus.VERIFIED_OWNER) {
            this.verificationStatus = EVerificationStatus.DISPUTED;
            this.updatedAt = LocalDateTime.now();
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
        this.lastSyncedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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
        this.lastSyncedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 인증된 상태인지 확인
     */
    public boolean isVerified() {
        return verificationStatus == EVerificationStatus.VERIFIED_OWNER;
    }

    /**
     * 인증 가능한 상태인지 확인
     */
    public boolean canStartVerification() {
        return verificationStatus == EVerificationStatus.UNVERIFIED_CLAIMED
                || verificationStatus == EVerificationStatus.DISPUTED;
    }

    /**
     * 파티 활동 가능한 상태인지 확인
     */
    public boolean canParticipateInParty() {
        return verificationStatus == EVerificationStatus.VERIFIED_OWNER;
    }
}
