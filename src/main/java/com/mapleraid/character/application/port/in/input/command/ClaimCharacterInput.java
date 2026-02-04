package com.mapleraid.character.application.port.in.input.command;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.user.domain.UserId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ClaimCharacterInput extends SelfValidating<ClaimCharacterInput> {

    @NotNull(message = "유저 아이디는 필수입니다.")
    private final UserId userId;

    @NotBlank(message = "캐릭터 이름은 필수입니다.")
    @Size(min = 2, max = 12, message = "캐릭터 이름은 2~12자 사이여야 합니다.")
    private final String characterName;

    @NotBlank(message = "월드 이름은 필수입니다.")
    private final String worldName;

    public ClaimCharacterInput(
            UserId userId,
            String characterName,
            String worldName
    ) {
        this.userId = userId;
        this.characterName = characterName;
        this.worldName = worldName;
        this.validateSelf();
    }

    public static ClaimCharacterInput of(
            UserId userId,
            String characterName,
            String worldName
    ) {
        return new ClaimCharacterInput(
                userId,
                characterName,
                worldName
        );
    }
}
