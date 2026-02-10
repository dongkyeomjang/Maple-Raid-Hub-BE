package com.mapleraid.notification.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationPreferenceJpaRepository extends JpaRepository<NotificationPreferenceJpaEntity, Long> {

    Optional<NotificationPreferenceJpaEntity> findByUserId(String userId);
}
