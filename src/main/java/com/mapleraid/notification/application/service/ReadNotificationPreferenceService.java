package com.mapleraid.notification.application.service;

import com.mapleraid.notification.application.port.in.usecase.ReadNotificationPreferenceUseCase;
import com.mapleraid.notification.application.port.out.NotificationPreferenceRepository;
import com.mapleraid.notification.domain.NotificationPreference;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReadNotificationPreferenceService implements ReadNotificationPreferenceUseCase {

    private final NotificationPreferenceRepository repository;

    @Override
    @Transactional(readOnly = true)
    public NotificationPreference execute(UserId userId) {
        return repository.findByUserId(userId)
                .orElse(NotificationPreference.createDefault(userId));
    }
}
