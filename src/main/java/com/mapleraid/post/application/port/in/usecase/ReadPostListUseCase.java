package com.mapleraid.post.application.port.in.usecase;

import com.mapleraid.post.application.port.in.input.query.ReadPostListInput;
import com.mapleraid.post.application.port.in.output.result.ReadPostListResult;

public interface ReadPostListUseCase {

    ReadPostListResult execute(ReadPostListInput input);
}
