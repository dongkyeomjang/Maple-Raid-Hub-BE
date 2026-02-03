package com.mapleraid.application.port.out;

import java.util.Map;
import java.util.Optional;

/**
 * Nexon Open API 클라이언트 포트
 * 서버사이드에서만 호출
 */
public interface NexonApiPort {

    /**
     * 캐릭터명과 월드로 OCID 조회
     */
    Optional<String> resolveOcid(String characterName, String worldName);

    /**
     * 캐릭터 기본 정보 조회
     */
    Optional<CharacterBasicInfo> getCharacterBasic(String ocid);

    /**
     * 캐릭터 장비 정보 조회 (인증용)
     */
    Optional<EquipmentInfo> getItemEquipment(String ocid);

    /**
     * 캐릭터 심볼 장비 조회 (인증용)
     */
    Optional<SymbolEquipmentInfo> getSymbolEquipment(String ocid);

    /**
     * 캐릭터 스탯 조회 (전투력 포함)
     */
    Optional<CharacterStatInfo> getCharacterStat(String ocid);

    /**
     * 프리셋별 캐릭터 스탯 조회
     *
     * @param ocid     캐릭터 OCID
     * @param presetNo 프리셋 번호 (1, 2, 3)
     */
    Optional<CharacterStatInfo> getCharacterStatByPreset(String ocid, int presetNo);

    /**
     * 프리셋 1~3 중 최대 전투력 스탯 조회
     */
    Optional<CharacterStatInfo> getMaxCombatPowerStat(String ocid);

    /**
     * 캐릭터 기본 정보 DTO
     */
    record CharacterBasicInfo(
            String characterName,
            String worldName,
            String characterClass,
            int characterLevel,
            String characterImage
    ) {
    }

    /**
     * 장비 정보 DTO
     */
    record EquipmentInfo(
            String date,
            Map<String, EquipmentSlot> slots
    ) {
    }

    /**
     * 장비 슬롯 DTO
     */
    record EquipmentSlot(
            String itemName,
            String itemIcon,
            Map<String, Object> details
    ) {
    }

    /**
     * 심볼 장비 정보 DTO
     */
    record SymbolEquipmentInfo(
            java.util.List<SymbolInfo> symbols
    ) {
    }

    /**
     * 개별 심볼 정보 DTO
     */
    record SymbolInfo(
            String symbolName,
            String symbolIcon,
            int symbolLevel,
            int symbolGrowthCount,
            int symbolRequireGrowthCount
    ) {
    }

    /**
     * 캐릭터 스탯 정보 DTO
     */
    record CharacterStatInfo(
            long combatPower
    ) {
    }
}
