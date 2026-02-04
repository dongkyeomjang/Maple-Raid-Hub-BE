package com.mapleraid.character.application.port.in.input.query;

import com.mapleraid.character.domain.CharacterId;
import lombok.Getter;

@Getter
public class ReadPendingChallengeInput {

    private final CharacterId characterId;

    public ReadPendingChallengeInput(
            CharacterId characterId
    ) {
        this.characterId = characterId;
    }

    public static ReadPendingChallengeInput of(
            CharacterId characterId
    ) {
        return new ReadPendingChallengeInput(
                characterId
        );
    }
}
