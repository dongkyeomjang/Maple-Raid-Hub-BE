package com.mapleraid.post.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import lombok.Getter;

@Getter
public class ReadGuestCharacterInfoResult extends SelfValidating<ReadGuestCharacterInfoResult> {

    private final String characterName;
    private final String worldName;
    private final String worldGroup;
    private final String characterClass;
    private final int characterLevel;
    private final String characterImageUrl;
    private final long combatPower;
    private final String equipmentJson;

    public ReadGuestCharacterInfoResult(String characterName, String worldName, String worldGroup,
                                        String characterClass, int characterLevel,
                                        String characterImageUrl, long combatPower,
                                        String equipmentJson) {
        this.characterName = characterName;
        this.worldName = worldName;
        this.worldGroup = worldGroup;
        this.characterClass = characterClass;
        this.characterLevel = characterLevel;
        this.characterImageUrl = characterImageUrl;
        this.combatPower = combatPower;
        this.equipmentJson = equipmentJson;
        this.validateSelf();
    }
}
