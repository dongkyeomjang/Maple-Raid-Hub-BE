package com.mapleraid.post.application.port.in.usecase;

import com.mapleraid.post.application.port.in.input.command.VerifyGuestPasswordInput;

public interface VerifyGuestPasswordUseCase {

    void execute(VerifyGuestPasswordInput input);
}
