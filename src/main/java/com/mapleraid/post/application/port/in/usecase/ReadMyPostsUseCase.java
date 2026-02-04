package com.mapleraid.post.application.port.in.usecase;

import com.mapleraid.post.application.port.in.input.query.ReadMyPostsInput;
import com.mapleraid.post.application.port.in.output.result.ReadMyPostsResult;

public interface ReadMyPostsUseCase {

    ReadMyPostsResult execute(ReadMyPostsInput input);
}
