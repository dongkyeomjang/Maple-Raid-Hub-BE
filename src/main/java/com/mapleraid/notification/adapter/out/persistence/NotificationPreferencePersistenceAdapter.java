package com.mapleraid.notification.adapter.out.persistence;

import com.mapleraid.notification.adapter.out.persistence.jpa.NotificationPreferenceJpaEntity;
import com.mapleraid.notification.adapter.out.persistence.jpa.NotificationPreferenceJpaRepository;
import com.mapleraid.notification.application.port.out.NotificationPreferenceRepository;
import com.mapleraid.notification.domain.NotificationPreference;
import com.mapleraid.user.domain.UserId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class NotificationPreferencePersistenceAdapter implements NotificationPreferenceRepository {

    private final NotificationPreferenceJpaRepository jpaRepository;

    public NotificationPreferencePersistenceAdapter(NotificationPreferenceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public NotificationPreference save(NotificationPreference preference) {
        NotificationPreferenceJpaEntity entity = NotificationPreferenceJpaEntity.fromDomain(preference);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<NotificationPreference> findByUserId(UserId userId) {
        return jpaRepository.findByUserId(userId.getValue().toString())
                .map(NotificationPreferenceJpaEntity::toDomain);
    }
}
