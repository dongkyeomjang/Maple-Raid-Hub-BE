package com.mapleraid.chat.application.port.in.usecase;

import com.mapleraid.chat.application.port.in.input.query.ReadMyDmRoomsInput;
import com.mapleraid.chat.application.port.in.output.result.ReadMyDmRoomsResult;
import com.mapleraid.core.annotation.bean.UseCase;

@UseCase
public interface ReadMyDmRoomsUseCase {
    ReadMyDmRoomsResult execute(ReadMyDmRoomsInput input);
}
