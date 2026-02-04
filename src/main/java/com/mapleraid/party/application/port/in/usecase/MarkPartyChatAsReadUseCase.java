package com.mapleraid.party.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.party.application.port.in.input.command.MarkPartyChatAsReadInput;

@UseCase
public interface MarkPartyChatAsReadUseCase {

    void execute(MarkPartyChatAsReadInput input);
}
