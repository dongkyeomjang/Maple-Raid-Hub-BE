package com.mapleraid.notification.application.port.in.usecase;

import com.mapleraid.user.domain.UserId;

public interface UnlinkDiscordUseCase {

    void execute(UserId userId);
}
