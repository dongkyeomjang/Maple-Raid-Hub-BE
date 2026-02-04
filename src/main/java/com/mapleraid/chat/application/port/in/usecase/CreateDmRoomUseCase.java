package com.mapleraid.chat.application.port.in.usecase;

import com.mapleraid.chat.application.port.in.input.command.CreateDmRoomInput;
import com.mapleraid.chat.application.port.in.output.result.CreateDmRoomResult;
import com.mapleraid.core.annotation.bean.UseCase;

@UseCase
public interface CreateDmRoomUseCase {
    CreateDmRoomResult execute(CreateDmRoomInput input);
}
