package com.mapleraid.post.application.port.in.usecase;

import com.mapleraid.post.application.port.in.input.command.UpdatePostInput;
import com.mapleraid.post.application.port.in.output.result.UpdatePostResult;

public interface UpdatePostUseCase {

    UpdatePostResult execute(UpdatePostInput input);
}
