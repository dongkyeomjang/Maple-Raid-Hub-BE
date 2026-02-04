package com.mapleraid.character.application.port.in.usecase;

import com.mapleraid.character.application.port.in.input.command.ClaimCharacterInput;
import com.mapleraid.character.application.port.in.output.result.ClaimCharacterResult;
import com.mapleraid.core.annotation.bean.UseCase;

@UseCase
public interface ClaimCharacterUseCase {

    ClaimCharacterResult execute(ClaimCharacterInput input);
}
