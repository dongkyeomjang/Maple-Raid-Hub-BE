package com.mapleraid.security.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.security.application.port.in.input.command.UpdateNicknameInput;
import com.mapleraid.security.application.port.in.output.result.UpdateNicknameResult;

@UseCase
public interface UpdateNicknameUseCase {

    UpdateNicknameResult execute(UpdateNicknameInput input);
}
