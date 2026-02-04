package com.mapleraid.chat.application.port.in.usecase;

import com.mapleraid.chat.application.port.in.input.query.ReadDmRoomInput;
import com.mapleraid.chat.application.port.in.output.result.ReadDmRoomResult;
import com.mapleraid.core.annotation.bean.UseCase;

@UseCase
public interface ReadDmRoomUseCase {
    ReadDmRoomResult execute(ReadDmRoomInput input);
}
