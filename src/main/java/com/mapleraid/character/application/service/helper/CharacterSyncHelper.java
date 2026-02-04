package com.mapleraid.character.application.service.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.external.application.port.out.NexonApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CharacterSyncHelper {

    private static final Duration SYNC_INTERVAL = Duration.ofHours(24);

    private final CharacterRepository characterRepository;
    private final NexonApiPort nexonApiPort;
    private final ObjectMapper objectMapper;

    /**
     * 동기화가 필요한지 확인
     */
    public boolean needsSync(Character character) {
        LocalDateTime lastSyncedAt = character.getLastSyncedAt();
        if (lastSyncedAt == null) {
            return true;
        }
        return Duration.between(lastSyncedAt, LocalDateTime.now()).compareTo(SYNC_INTERVAL) > 0;
    }

    /**
     * 캐릭터 정보 동기화
     */
    public Character sync(Character character) {
        String ocid = character.getOcid();

        NexonApiPort.CharacterBasicInfo basicInfo = nexonApiPort.getCharacterBasic(ocid)
                .orElseThrow(() -> new CommonException(ErrorCode.CHARACTER_INFO_UNAVAILABLE));

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

    /**
     * 필요한 경우에만 동기화
     */
    public Character syncIfNeeded(Character character) {
        if (needsSync(character)) {
            try {
                return sync(character);
            } catch (Exception e) {
                return character;
            }
        }
        return character;
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
