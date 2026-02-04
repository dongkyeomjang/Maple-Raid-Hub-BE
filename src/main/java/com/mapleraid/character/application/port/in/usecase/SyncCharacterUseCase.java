package com.mapleraid.character.application.port.in.usecase;

import com.mapleraid.character.application.port.in.input.command.SyncCharacterInput;
import com.mapleraid.character.application.port.in.output.result.SyncCharacterResult;
import com.mapleraid.core.annotation.bean.UseCase;

@UseCase
public interface SyncCharacterUseCase {

    SyncCharacterResult execute(SyncCharacterInput input);
}
