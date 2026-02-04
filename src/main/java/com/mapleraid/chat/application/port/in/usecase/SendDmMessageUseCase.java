package com.mapleraid.chat.application.port.in.usecase;

import com.mapleraid.chat.application.port.in.input.command.SendDmMessageInput;
import com.mapleraid.chat.application.port.in.output.result.SendDmMessageResult;
import com.mapleraid.core.annotation.bean.UseCase;

@UseCase
public interface SendDmMessageUseCase {
    SendDmMessageResult execute(SendDmMessageInput input);
}
