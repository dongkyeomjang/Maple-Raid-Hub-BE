package com.mapleraid.post.adapter.in.web.dto.response;

import com.mapleraid.post.application.port.in.output.result.ReadGuestCharacterInfoResult;

public record GuestCharacterInfoResponseDto(
        String characterName,
        String worldName,
        String worldGroup,
        String characterClass,
        int characterLevel,
        String characterImageUrl,
        long combatPower,
        String equipmentJson
) {
    public static GuestCharacterInfoResponseDto from(ReadGuestCharacterInfoResult result) {
        return new GuestCharacterInfoResponseDto(
                result.getCharacterName(),
                result.getWorldName(),
                result.getWorldGroup(),
                result.getCharacterClass(),
                result.getCharacterLevel(),
                result.getCharacterImageUrl(),
                result.getCombatPower(),
                result.getEquipmentJson()
        );
    }
}
