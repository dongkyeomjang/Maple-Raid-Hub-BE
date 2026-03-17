package com.mapleraid.security.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.security.application.port.in.input.command.ResetPasswordInput;

@UseCase
public interface ResetPasswordUseCase {

    void execute(ResetPasswordInput input);
}
