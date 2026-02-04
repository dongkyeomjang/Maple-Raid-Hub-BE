package com.mapleraid.chat.application.port.in.usecase;

import com.mapleraid.chat.application.port.in.input.command.MarkDmAsReadInput;
import com.mapleraid.core.annotation.bean.UseCase;

@UseCase
public interface MarkDmAsReadUseCase {
    void execute(MarkDmAsReadInput input);
}
