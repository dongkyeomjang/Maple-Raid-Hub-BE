package com.mapleraid.post.application.port.in.usecase;

import com.mapleraid.post.application.port.in.input.command.RejectApplicationInput;
import com.mapleraid.post.application.port.in.output.result.RejectApplicationResult;

public interface RejectApplicationUseCase {

    RejectApplicationResult execute(RejectApplicationInput input);
}
