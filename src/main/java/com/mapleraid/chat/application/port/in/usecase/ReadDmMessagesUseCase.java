package com.mapleraid.chat.application.port.in.usecase;

import com.mapleraid.chat.application.port.in.input.query.ReadDmMessagesInput;
import com.mapleraid.chat.application.port.in.output.result.ReadDmMessagesResult;
import com.mapleraid.core.annotation.bean.UseCase;

@UseCase
public interface ReadDmMessagesUseCase {
    ReadDmMessagesResult execute(ReadDmMessagesInput input);
}
