package com.mapleraid.post.application.port.in.usecase;

import com.mapleraid.post.application.port.in.input.command.ClosePostInput;
import com.mapleraid.post.application.port.in.output.result.ClosePostResult;

public interface ClosePostUseCase {

    ClosePostResult execute(ClosePostInput input);
}
