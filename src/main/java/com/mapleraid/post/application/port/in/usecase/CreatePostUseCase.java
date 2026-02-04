package com.mapleraid.post.application.port.in.usecase;

import com.mapleraid.post.application.port.in.input.command.CreatePostInput;
import com.mapleraid.post.application.port.in.output.result.CreatePostResult;

public interface CreatePostUseCase {

    CreatePostResult execute(CreatePostInput input);
}
