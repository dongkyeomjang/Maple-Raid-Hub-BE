package com.mapleraid.character.application.port.in.input.command;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class DeleteCharacterInput {

    private final UserId requesterId;

    private final CharacterId characterId;

    public DeleteCharacterInput(
            UserId requesterId,
            CharacterId characterId
    ) {
        this.requesterId = requesterId;
        this.characterId = characterId;
    }

    public static DeleteCharacterInput of(
            UserId requesterId,
            CharacterId characterId
    ) {
        return new DeleteCharacterInput(
                requesterId,
                characterId
        );
    }
}
