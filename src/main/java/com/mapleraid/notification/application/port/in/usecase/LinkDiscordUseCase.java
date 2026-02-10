package com.mapleraid.notification.application.port.in.usecase;

import com.mapleraid.user.domain.UserId;

public interface LinkDiscordUseCase {

    void execute(UserId userId, String code);
}
