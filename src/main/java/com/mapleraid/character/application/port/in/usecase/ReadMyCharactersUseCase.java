package com.mapleraid.character.application.port.in.usecase;

import com.mapleraid.character.application.port.in.input.query.ReadMyCharactersInput;
import com.mapleraid.character.application.port.in.output.result.ReadMyCharactersResult;
import com.mapleraid.core.annotation.bean.UseCase;

@UseCase
public interface ReadMyCharactersUseCase {

    ReadMyCharactersResult execute(ReadMyCharactersInput input);
}
