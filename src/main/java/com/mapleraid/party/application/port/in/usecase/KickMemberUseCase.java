package com.mapleraid.party.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.party.application.port.in.input.command.KickMemberInput;

@UseCase
public interface KickMemberUseCase {

    void execute(KickMemberInput input);
}
