package com.mapleraid.security.application.port.in.input.command;

import com.mapleraid.core.dto.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginInput extends SelfValidating<LoginInput> {

    @NotBlank(message = "아이디는 필수입니다.")
    private final String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private final String password;

    public LoginInput(
            String username,
            String password
    ) {
        this.username = username;
        this.password = password;
        this.validateSelf();
    }

    public static LoginInput of(
            String username,
            String password
    ) {
        return new LoginInput(
                username,
                password
        );
    }
}
