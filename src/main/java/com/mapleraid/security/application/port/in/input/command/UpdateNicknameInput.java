package com.mapleraid.security.application.port.in.input.command;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.user.domain.UserId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateNicknameInput extends SelfValidating<UpdateNicknameInput> {

    @NotNull(message = "유저 아이디는 필수입니다.")
    private final UserId userId;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    private final String nickname;

    public UpdateNicknameInput(
            UserId userId,
            String nickname
    ) {
        this.userId = userId;
        this.nickname = nickname;
        this.validateSelf();
    }

    public static UpdateNicknameInput of(
            UserId userId,
            String nickname
    ) {
        return new UpdateNicknameInput(
                userId,
                nickname
        );
    }
}
