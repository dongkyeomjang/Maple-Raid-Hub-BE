package com.mapleraid.post.application.port.in.usecase;

import com.mapleraid.post.application.port.in.input.command.AcceptApplicationInput;
import com.mapleraid.post.application.port.in.output.result.AcceptApplicationResult;

public interface AcceptApplicationUseCase {

    AcceptApplicationResult execute(AcceptApplicationInput input);
}
