package com.mapleraid.post.application.port.in.usecase;

import com.mapleraid.post.application.port.in.input.command.ApplyToPostInput;
import com.mapleraid.post.application.port.in.output.result.ApplyToPostResult;

public interface ApplyToPostUseCase {

    ApplyToPostResult execute(ApplyToPostInput input);
}
