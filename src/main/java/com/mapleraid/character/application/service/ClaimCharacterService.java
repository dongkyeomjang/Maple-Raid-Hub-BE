package com.mapleraid.character.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapleraid.character.application.port.in.input.command.ClaimCharacterInput;
import com.mapleraid.character.application.port.in.output.result.ClaimCharacterResult;
import com.mapleraid.character.application.port.in.usecase.ClaimCharacterUseCase;
import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.character.domain.resolver.WorldGroupResolver;
import com.mapleraid.character.domain.type.EVerificationStatus;
import com.mapleraid.character.domain.type.EWorldGroup;
import com.mapleraid.core.constant.Constants;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.external.application.port.out.NexonApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClaimCharacterService implements ClaimCharacterUseCase {

    private final CharacterRepository characterRepository;
    private final NexonApiPort nexonApiPort;
    private final WorldGroupResolver worldGroupResolver;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ClaimCharacterResult execute(ClaimCharacterInput input) {
        // 월드 유효성 검증
        if (!worldGroupResolver.isValidWorld(input.getWorldName())) {
            throw new CommonException(ErrorCode.INVALID_WORLD);
        }

        // 이미 본인이 등록한 캐릭터인지 확인
        if (characterRepository.existsByOwnerIdAndNameAndWorld(
                input.getUserId(),
                input.getCharacterName(),
                input.getWorldName())
        ) {
            throw new CommonException(ErrorCode.CHARACTER_ALREADY_REGISTERED);
        }

        // 이미 인증된 캐릭터인지 확인
        if (characterRepository.existsByNameAndWorldAndStatus(
                input.getCharacterName(), input.getWorldName(), EVerificationStatus.VERIFIED_OWNER)) {
            throw new CommonException(ErrorCode.CHARACTER_ALREADY_VERIFIED);
        }

        // Nexon API로 캐릭터 존재 확인 및 정보 조회
        String ocid = nexonApiPort.resolveOcid(input.getCharacterName(), input.getWorldName())
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_CHARACTER));

        NexonApiPort.CharacterBasicInfo basicInfo = nexonApiPort.getCharacterBasic(ocid)
                .orElseThrow(() -> new CommonException(ErrorCode.CHARACTER_INFO_UNAVAILABLE));

        // 월드명 검증 - API가 반환한 실제 월드와 사용자 입력 비교
        if (!basicInfo.worldName().equals(input.getWorldName())) {
            throw new CommonException(ErrorCode.CHARACTER_WORLD_MISMATCH);
        }

        // 레벨 검증
        if (basicInfo.characterLevel() < Constants.MIN_CHARACTER_LEVEL) {
            throw new CommonException(ErrorCode.CHARACTER_LEVEL_TOO_LOW);
        }

        // 월드 그룹 결정 (API 실제 월드명 기준)
        EWorldGroup EWorldGroup = worldGroupResolver.resolve(basicInfo.worldName());

        // 캐릭터 생성 (API 실제 값 사용)
        Character character = Character.claim(
                CharacterId.generate(),
                basicInfo.characterName(),
                basicInfo.worldName(),
                EWorldGroup,
                input.getUserId(),
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

        Character savedCharacter = characterRepository.save(character);

        return ClaimCharacterResult.of(
                savedCharacter.getId().getValue().toString(),
                savedCharacter.getCharacterName(),
                savedCharacter.getWorldName(),
                savedCharacter.getEWorldGroup(),
                savedCharacter.getCharacterClass(),
                savedCharacter.getCharacterLevel(),
                savedCharacter.getCharacterImageUrl(),
                savedCharacter.getCombatPower(),
                savedCharacter.getEquipmentJson(),
                savedCharacter.getVerificationStatus(),
                savedCharacter.getClaimedAt(),
                savedCharacter.getVerifiedAt(),
                savedCharacter.getLastSyncedAt()
        );
    }


    /**
     * ============================= PRIVATE METHODS =============================
     */
    private String convertEquipmentToJson(NexonApiPort.EquipmentInfo equipmentInfo) {
        try {
            return objectMapper.writeValueAsString(equipmentInfo);
        } catch (Exception e) {
            return null;
        }
    }
}

