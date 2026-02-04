package com.mapleraid.security.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.security.application.port.in.input.command.CompleteOauthSignupInput;
import com.mapleraid.security.application.port.in.output.result.CompleteOauthSignupResult;

@UseCase
public interface CompleteOauthSignupUseCase {

    CompleteOauthSignupResult execute(CompleteOauthSignupInput input);
}
