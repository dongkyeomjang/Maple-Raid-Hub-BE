package com.mapleraid.notification.application.port.out;

import com.mapleraid.notification.domain.NotificationPreference;
import com.mapleraid.user.domain.UserId;

import java.util.Optional;

public interface NotificationPreferenceRepository {

    NotificationPreference save(NotificationPreference preference);

    Optional<NotificationPreference> findByUserId(UserId userId);
}
