package com.mapleraid.character.application.port.in.usecase;

import com.mapleraid.character.application.port.in.input.query.ReadPendingChallengeInput;
import com.mapleraid.character.application.port.in.output.result.ReadPendingChallengeResult;
import com.mapleraid.core.annotation.bean.UseCase;

@UseCase
public interface ReadPendingChallengeUseCase {

    ReadPendingChallengeResult execute(ReadPendingChallengeInput input);
}
