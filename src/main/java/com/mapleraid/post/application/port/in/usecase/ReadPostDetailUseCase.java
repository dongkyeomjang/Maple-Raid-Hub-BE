package com.mapleraid.post.application.port.in.usecase;

import com.mapleraid.post.application.port.in.input.query.ReadPostDetailInput;
import com.mapleraid.post.application.port.in.output.result.ReadPostDetailResult;

public interface ReadPostDetailUseCase {

    ReadPostDetailResult execute(ReadPostDetailInput input);
}
