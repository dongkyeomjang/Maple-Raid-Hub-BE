package com.mapleraid.party.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.party.application.port.in.input.command.SendPartyChatMessageInput;
import com.mapleraid.party.application.port.in.output.result.SendPartyChatMessageResult;

@UseCase
public interface SendPartyChatMessageUseCase {

    SendPartyChatMessageResult execute(SendPartyChatMessageInput input);
}
