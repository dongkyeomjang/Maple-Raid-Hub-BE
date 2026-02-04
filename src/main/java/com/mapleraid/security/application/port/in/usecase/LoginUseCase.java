package com.mapleraid.security.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.security.application.port.in.input.command.LoginInput;
import com.mapleraid.security.application.port.in.output.result.LoginResult;

@UseCase
public interface LoginUseCase {

    LoginResult execute(LoginInput input);
}
