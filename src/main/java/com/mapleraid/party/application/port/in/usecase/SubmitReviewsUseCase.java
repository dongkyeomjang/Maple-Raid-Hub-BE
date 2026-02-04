package com.mapleraid.party.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.party.application.port.in.input.command.SubmitReviewsInput;
import com.mapleraid.party.application.port.in.output.result.SubmitReviewsResult;

@UseCase
public interface SubmitReviewsUseCase {

    SubmitReviewsResult execute(SubmitReviewsInput input);
}
