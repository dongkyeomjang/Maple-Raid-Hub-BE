package com.mapleraid.character.application.port.in.input.command;

import com.mapleraid.character.domain.CharacterId;
import lombok.Getter;

@Getter
public class SyncCharacterInput {

    private final CharacterId characterId;

    public SyncCharacterInput(
            CharacterId characterId
    ) {
        this.characterId = characterId;
    }

    public static SyncCharacterInput of(
            CharacterId characterId
    ) {
        return new SyncCharacterInput(
                characterId
        );
    }
}
