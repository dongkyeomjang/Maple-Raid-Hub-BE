package com.mapleraid.character.application.port.in.input.command;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class CreateVerificationChallengeInput {

    private final CharacterId characterId;

    private final UserId requesterId;

    public CreateVerificationChallengeInput(
            CharacterId characterId,
            UserId requesterId
    ) {
        this.characterId = characterId;
        this.requesterId = requesterId;
    }

    public static CreateVerificationChallengeInput of(
            CharacterId characterId,
            UserId requesterId
    ) {
        return new CreateVerificationChallengeInput(
                characterId,
                requesterId
        );
    }
}
