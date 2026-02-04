package com.mapleraid.party.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.party.application.port.in.input.query.ReadPartyChatMessagesInput;
import com.mapleraid.party.application.port.in.output.result.ReadPartyChatMessagesResult;

@UseCase
public interface ReadPartyChatMessagesUseCase {

    ReadPartyChatMessagesResult execute(ReadPartyChatMessagesInput input);
}
