package com.mapleraid.security.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.security.application.port.in.input.command.SignupInput;
import com.mapleraid.security.application.port.in.output.result.SignupResult;

@UseCase
public interface SignupUseCase {

    SignupResult execute(SignupInput input);
}
