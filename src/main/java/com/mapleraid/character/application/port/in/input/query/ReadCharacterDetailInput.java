package com.mapleraid.character.application.port.in.input.query;

import com.mapleraid.character.domain.CharacterId;
import lombok.Getter;

@Getter
public class ReadCharacterDetailInput {

    private final CharacterId characterId;

    public ReadCharacterDetailInput(
            CharacterId characterId
    ) {
        this.characterId = characterId;
    }

    public static ReadCharacterDetailInput of(
            CharacterId characterId
    ) {
        return new ReadCharacterDetailInput(
                characterId
        );
    }
}
