package com.mapleraid.notification.application.port.in.usecase;

import com.mapleraid.user.domain.UserId;

public interface GetDiscordStatusUseCase {

    record DiscordStatus(boolean linked, String discordUsername) {}

    DiscordStatus execute(UserId userId);
}
