package com.mapleraid.post.application.port.in.usecase;

import com.mapleraid.post.application.port.in.input.query.ReadPostApplicationsInput;
import com.mapleraid.post.application.port.in.output.result.ReadPostApplicationsResult;

public interface ReadPostApplicationsUseCase {

    ReadPostApplicationsResult execute(ReadPostApplicationsInput input);
}
