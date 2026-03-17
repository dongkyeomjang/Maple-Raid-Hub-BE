package com.mapleraid.security.application.port.in.input.query;

import com.mapleraid.core.dto.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ReadPendingRecoveryChallengeInput extends SelfValidating<ReadPendingRecoveryChallengeInput> {

    @NotBlank(message = "캐릭터명은 필수입니다.")
    private final String characterName;

    @NotBlank(message = "월드명은 필수입니다.")
    private final String worldName;

    public ReadPendingRecoveryChallengeInput(String characterName, String worldName) {
        this.characterName = characterName;
        this.worldName = worldName;
        this.validateSelf();
    }

    public static ReadPendingRecoveryChallengeInput of(String characterName, String worldName) {
        return new ReadPendingRecoveryChallengeInput(characterName, worldName);
    }
}
