package com.mapleraid.post.application.port.in.usecase;

import com.mapleraid.post.application.port.in.input.query.ReadGuestCharacterInfoInput;
import com.mapleraid.post.application.port.in.output.result.ReadGuestCharacterInfoResult;

public interface ReadGuestCharacterInfoUseCase {

    ReadGuestCharacterInfoResult execute(ReadGuestCharacterInfoInput input);
}
