package com.mapleraid.post.application.port.in.usecase;

import com.mapleraid.post.application.port.in.input.command.WithdrawApplicationInput;
import com.mapleraid.post.application.port.in.output.result.WithdrawApplicationResult;

public interface WithdrawApplicationUseCase {

    WithdrawApplicationResult execute(WithdrawApplicationInput input);
}
