package com.mapleraid.notification.application.port.in.usecase;

import com.mapleraid.notification.domain.NotificationPreference;
import com.mapleraid.user.domain.UserId;

public interface ReadNotificationPreferenceUseCase {

    NotificationPreference execute(UserId userId);
}
