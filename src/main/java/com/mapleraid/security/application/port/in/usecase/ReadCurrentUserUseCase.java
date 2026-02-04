package com.mapleraid.security.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.security.application.port.in.input.query.ReadCurrentUserInput;
import com.mapleraid.security.application.port.in.output.result.ReadCurrentUserResult;

@UseCase
public interface ReadCurrentUserUseCase {

    ReadCurrentUserResult execute(ReadCurrentUserInput input);
}
