package com.mapleraid.security.application.port.in.input.command;

import com.mapleraid.core.dto.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ResetPasswordInput extends SelfValidating<ResetPasswordInput> {

    @NotBlank(message = "복구 토큰은 필수입니다.")
    private final String recoveryToken;

    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Size(min = 6, max = 50, message = "비밀번호는 6~50자 사이여야 합니다.")
    private final String newPassword;

    public ResetPasswordInput(String recoveryToken, String newPassword) {
        this.recoveryToken = recoveryToken;
        this.newPassword = newPassword;
        this.validateSelf();
    }

    public static ResetPasswordInput of(String recoveryToken, String newPassword) {
        return new ResetPasswordInput(recoveryToken, newPassword);
    }
}
