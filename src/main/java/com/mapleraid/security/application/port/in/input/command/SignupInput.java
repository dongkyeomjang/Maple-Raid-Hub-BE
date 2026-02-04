package com.mapleraid.security.application.port.in.input.command;

import com.mapleraid.core.dto.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupInput extends SelfValidating<SignupInput> {

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4~20자 사이여야 합니다.")
    private final String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 6, max = 50, message = "비밀번호는 6~50자 사이여야 합니다.")
    private final String password;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    private final String nickname;

    public SignupInput(
            String username,
            String password,
            String nickname
    ) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.validateSelf();
    }

    public static SignupInput of(
            String username,
            String password,
            String nickname
    ) {
        return new SignupInput(
                username,
                password,
                nickname
        );
    }
}
