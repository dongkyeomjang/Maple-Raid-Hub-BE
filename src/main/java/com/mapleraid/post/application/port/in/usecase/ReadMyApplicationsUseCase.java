package com.mapleraid.post.application.port.in.usecase;

import com.mapleraid.post.application.port.in.input.query.ReadMyApplicationsInput;
import com.mapleraid.post.application.port.in.output.result.ReadMyApplicationsResult;

public interface ReadMyApplicationsUseCase {

    ReadMyApplicationsResult execute(ReadMyApplicationsInput input);
}
