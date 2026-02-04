package com.mapleraid.post.application.port.in.usecase;

import com.mapleraid.post.application.port.in.input.command.DeletePostInput;

public interface DeletePostUseCase {

    void execute(DeletePostInput input);
}
