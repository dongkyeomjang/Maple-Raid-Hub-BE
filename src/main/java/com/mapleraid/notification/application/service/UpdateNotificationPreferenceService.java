package com.mapleraid.notification.application.service;

import com.mapleraid.notification.application.port.in.usecase.UpdateNotificationPreferenceUseCase;
import com.mapleraid.notification.application.port.out.NotificationPreferenceRepository;
import com.mapleraid.notification.domain.NotificationPreference;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateNotificationPreferenceService implements UpdateNotificationPreferenceUseCase {

    private final NotificationPreferenceRepository repository;

    @Override
    @Transactional
    public NotificationPreference execute(UserId userId, Boolean notifyApplicationReceived,
                                          Boolean notifyApplicationAccepted,
                                          Boolean notifyApplicationRejected,
                                          Boolean notifyDmReceived) {
        NotificationPreference pref = repository.findByUserId(userId)
                .orElseGet(() -> repository.save(NotificationPreference.createDefault(userId)));

        pref.update(notifyApplicationReceived, notifyApplicationAccepted,
                notifyApplicationRejected, notifyDmReceived);

        return repository.save(pref);
    }
}
