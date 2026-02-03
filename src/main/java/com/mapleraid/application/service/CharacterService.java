package com.mapleraid.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapleraid.application.port.out.CharacterRepository;
import com.mapleraid.application.port.out.NexonApiPort;
import com.mapleraid.domain.character.Character;
import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.character.VerificationStatus;
import com.mapleraid.domain.character.WorldGroup;
import com.mapleraid.domain.character.WorldGroupResolver;
import com.mapleraid.domain.common.DomainException;
import com.mapleraid.domain.user.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CharacterService {

    private static final int MIN_CHARACTER_LEVEL = 260;
    private static final Duration SYNC_INTERVAL = Duration.ofHours(24);

    private final CharacterRepository characterRepository;
    private final NexonApiPort nexonApiPort;
    private final WorldGroupResolver worldGroupResolver;
    private final ObjectMapper objectMapper;

    public CharacterService(CharacterRepository characterRepository,
                            NexonApiPort nexonApiPort,
                            WorldGroupResolver worldGroupResolver,
                            ObjectMapper objectMapper) {
        this.characterRepository = characterRepository;
        this.nexonApiPort = nexonApiPort;
        this.worldGroupResolver = worldGroupResolver;
        this.objectMapper = objectMapper;
    }

    /**
     * 캐릭터 등록 (Claim)
     */
    public Character claimCharacter(UserId userId, String characterName, String worldName) {
        // 월드 유효성 검증
        if (!worldGroupResolver.isValidWorld(worldName)) {
            throw new DomainException("CHARACTER_INVALID_WORLD",
                    "유효하지 않은 월드입니다: " + worldName);
        }

        // 이미 본인이 등록한 캐릭터인지 확인
        if (characterRepository.existsByOwnerIdAndNameAndWorld(userId, characterName, worldName)) {
            throw new DomainException("CHARACTER_ALREADY_REGISTERED",
                    "이미 등록한 캐릭터입니다.",
                    Map.of("characterName", characterName, "worldName", worldName));
        }

        // 이미 인증된 캐릭터인지 확인
        if (characterRepository.existsByNameAndWorldAndStatus(
                characterName, worldName, VerificationStatus.VERIFIED_OWNER)) {
            throw new DomainException("CHARACTER_ALREADY_VERIFIED",
                    "이 캐릭터는 이미 다른 사용자가 인증했습니다.",
                    Map.of("characterName", characterName, "worldName", worldName));
        }

        // Nexon API로 캐릭터 존재 확인 및 정보 조회
        String ocid = nexonApiPort.resolveOcid(characterName, worldName)
                .orElseThrow(() -> new DomainException("CHARACTER_NOT_FOUND",
                        "해당 캐릭터를 찾을 수 없습니다. 캐릭터명과 월드를 확인해주세요."));

        NexonApiPort.CharacterBasicInfo basicInfo = nexonApiPort.getCharacterBasic(ocid)
                .orElseThrow(() -> new DomainException("CHARACTER_INFO_UNAVAILABLE",
                        "캐릭터 정보를 조회할 수 없습니다."));

        // 레벨 검증
        if (basicInfo.characterLevel() < MIN_CHARACTER_LEVEL) {
            throw new DomainException("CHARACTER_LEVEL_TOO_LOW",
                    String.format("레벨 %d 이상의 캐릭터만 등록할 수 있습니다.", MIN_CHARACTER_LEVEL),
                    Map.of("currentLevel", basicInfo.characterLevel(), "requiredLevel", MIN_CHARACTER_LEVEL));
        }

        // 월드 그룹 결정
        WorldGroup worldGroup = worldGroupResolver.resolve(worldName);

        // 캐릭터 생성
        Character character = Character.claim(
                CharacterId.generate(),
                characterName,
                worldName,
                worldGroup,
                userId,
                ocid
        );

        // 전투력 및 장비 정보 조회 (프리셋 1~3 중 최대 전투력 사용)
        long combatPower = nexonApiPort.getMaxCombatPowerStat(ocid)
                .map(NexonApiPort.CharacterStatInfo::combatPower)
                .orElse(0L);

        String equipmentJson = nexonApiPort.getItemEquipment(ocid)
                .map(this::convertEquipmentToJson)
                .orElse(null);

        // Nexon API 정보로 동기화 (전투력, 장비 포함)
        character.syncFromNexonApi(
                basicInfo.characterClass(),
                basicInfo.characterLevel(),
                basicInfo.characterImage(),
                ocid,
                combatPower,
                equipmentJson
        );

        return characterRepository.save(character);
    }

    /**
     * 내 캐릭터 목록 조회
     * 동기화가 필요한 캐릭터는 자동으로 업데이트
     */
    public List<Character> getMyCharacters(UserId userId) {
        List<Character> characters = characterRepository.findByOwnerId(userId);

        return characters.stream()
                .map(character -> {
                    if (needsSync(character)) {
                        try {
                            return syncCharacterInternal(character);
                        } catch (Exception e) {
                            // 동기화 실패 시 기존 데이터 반환
                            return character;
                        }
                    }
                    return character;
                })
                .toList();
    }

    /**
     * 캐릭터 상세 조회
     * lastSyncedAt이 null이거나 1시간 이상 지났으면 자동으로 동기화
     */
    public Character getCharacter(CharacterId characterId) {
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new DomainException("CHARACTER_NOT_FOUND",
                        "캐릭터를 찾을 수 없습니다."));

        if (needsSync(character)) {
            return syncCharacterInternal(character);
        }

        return character;
    }

    /**
     * 동기화가 필요한지 확인
     */
    private boolean needsSync(Character character) {
        Instant lastSyncedAt = character.getLastSyncedAt();
        if (lastSyncedAt == null) {
            return true;
        }
        return Duration.between(lastSyncedAt, Instant.now()).compareTo(SYNC_INTERVAL) > 0;
    }

    /**
     * 캐릭터 삭제
     */
    public void deleteCharacter(CharacterId characterId, UserId requesterId) {
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new DomainException("CHARACTER_NOT_FOUND",
                        "캐릭터를 찾을 수 없습니다."));

        if (!character.getOwnerId().equals(requesterId)) {
            throw new DomainException("CHARACTER_NOT_OWNER",
                    "본인의 캐릭터만 삭제할 수 있습니다.");
        }

        // TODO: 진행 중인 파티 확인

        characterRepository.delete(character);
    }

    /**
     * 인증된 캐릭터 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Character> getVerifiedCharacters(UserId userId) {
        return characterRepository.findByOwnerIdAndStatus(userId, VerificationStatus.VERIFIED_OWNER);
    }

    /**
     * 캐릭터 정보 동기화 (수동)
     */
    public Character syncCharacterInfo(CharacterId characterId) {
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new DomainException("CHARACTER_NOT_FOUND",
                        "캐릭터를 찾을 수 없습니다."));

        return syncCharacterInternal(character);
    }

    /**
     * 캐릭터 정보 동기화 (내부)
     */
    private Character syncCharacterInternal(Character character) {
        String ocid = character.getOcid();

        NexonApiPort.CharacterBasicInfo basicInfo = nexonApiPort.getCharacterBasic(ocid)
                .orElseThrow(() -> new DomainException("CHARACTER_INFO_UNAVAILABLE",
                        "캐릭터 정보를 조회할 수 없습니다."));

        // 전투력 및 장비 정보 조회 (프리셋 1~3 중 최대 전투력 사용)
        long combatPower = nexonApiPort.getMaxCombatPowerStat(ocid)
                .map(NexonApiPort.CharacterStatInfo::combatPower)
                .orElse(0L);

        String equipmentJson = nexonApiPort.getItemEquipment(ocid)
                .map(this::convertEquipmentToJson)
                .orElse(null);

        character.syncFromNexonApi(
                basicInfo.characterClass(),
                basicInfo.characterLevel(),
                basicInfo.characterImage(),
                ocid,
                combatPower,
                equipmentJson
        );

        return characterRepository.save(character);
    }

    private String convertEquipmentToJson(NexonApiPort.EquipmentInfo equipmentInfo) {
        try {
            return objectMapper.writeValueAsString(equipmentInfo);
        } catch (Exception e) {
            return null;
        }
    }
}
