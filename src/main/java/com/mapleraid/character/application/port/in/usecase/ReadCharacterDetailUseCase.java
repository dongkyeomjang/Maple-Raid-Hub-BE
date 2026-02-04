package com.mapleraid.character.application.port.in.usecase;

import com.mapleraid.character.application.port.in.input.query.ReadCharacterDetailInput;
import com.mapleraid.character.application.port.in.output.result.ReadCharacterDetailResult;
import com.mapleraid.core.annotation.bean.UseCase;

@UseCase
public interface ReadCharacterDetailUseCase {

    ReadCharacterDetailResult execute(ReadCharacterDetailInput input);
}
