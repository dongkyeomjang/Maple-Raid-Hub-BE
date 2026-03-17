package com.mapleraid.security.application.port.in.input.command;

import com.mapleraid.core.dto.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateRecoveryChallengeInput extends SelfValidating<CreateRecoveryChallengeInput> {

    @NotBlank(message = "캐릭터명은 필수입니다.")
    private final String characterName;

    @NotBlank(message = "월드명은 필수입니다.")
    private final String worldName;

    public CreateRecoveryChallengeInput(String characterName, String worldName) {
        this.characterName = characterName;
        this.worldName = worldName;
        this.validateSelf();
    }

    public static CreateRecoveryChallengeInput of(String characterName, String worldName) {
        return new CreateRecoveryChallengeInput(characterName, worldName);
    }
}
