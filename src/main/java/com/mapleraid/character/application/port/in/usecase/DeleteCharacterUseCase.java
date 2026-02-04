package com.mapleraid.character.application.port.in.usecase;

import com.mapleraid.character.application.port.in.input.command.DeleteCharacterInput;
import com.mapleraid.core.annotation.bean.UseCase;

@UseCase
public interface DeleteCharacterUseCase {

    void execute(DeleteCharacterInput input);
}
