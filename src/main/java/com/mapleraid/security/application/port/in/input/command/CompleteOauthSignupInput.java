package com.mapleraid.security.application.port.in.input.command;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.security.type.ESecurityProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CompleteOauthSignupInput extends SelfValidating<CompleteOauthSignupInput> {

    @NotBlank(message = "프로바이더 ID는 필수입니다.")
    private final String providerId;

    @NotNull(message = "프로바이더는 필수입니다.")
    private final ESecurityProvider provider;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    private final String nickname;

    public CompleteOauthSignupInput(
            String providerId,
            ESecurityProvider provider,
            String nickname
    ) {
        this.providerId = providerId;
        this.provider = provider;
        this.nickname = nickname;
        this.validateSelf();
    }

    public static CompleteOauthSignupInput of(
            String providerId,
            ESecurityProvider provider,
            String nickname
    ) {
        return new CompleteOauthSignupInput(
                providerId,
                provider,
                nickname
        );
    }
}
