package com.mapleraid.notification.application.port.in.usecase;

import com.mapleraid.notification.domain.NotificationPreference;
import com.mapleraid.user.domain.UserId;

public interface UpdateNotificationPreferenceUseCase {

    NotificationPreference execute(UserId userId, Boolean notifyApplicationReceived,
                                   Boolean notifyApplicationAccepted,
                                   Boolean notifyApplicationRejected,
                                   Boolean notifyDmReceived);
}
