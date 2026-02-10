package com.mapleraid.notification.application.port.in.usecase;

import com.mapleraid.user.domain.UserId;

public interface DismissDiscordPromptUseCase {

    void execute(UserId userId);
}
