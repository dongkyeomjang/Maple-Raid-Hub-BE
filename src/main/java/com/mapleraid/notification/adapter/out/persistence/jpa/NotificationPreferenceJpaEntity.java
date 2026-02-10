package com.mapleraid.notification.adapter.out.persistence.jpa;

import com.mapleraid.notification.domain.NotificationPreference;
import com.mapleraid.user.domain.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification_preferences")
public class NotificationPreferenceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true, length = 36)
    private String userId;

    @Column(name = "notify_application_received", columnDefinition = "boolean default true")
    private boolean notifyApplicationReceived = true;

    @Column(name = "notify_application_accepted", columnDefinition = "boolean default true")
    private boolean notifyApplicationAccepted = true;

    @Column(name = "notify_application_rejected", columnDefinition = "boolean default true")
    private boolean notifyApplicationRejected = true;

    @Column(name = "notify_dm_received", columnDefinition = "boolean default true")
    private boolean notifyDmReceived = true;

    public static NotificationPreferenceJpaEntity fromDomain(NotificationPreference pref) {
        NotificationPreferenceJpaEntity entity = new NotificationPreferenceJpaEntity();
        entity.id = pref.getId();
        entity.userId = pref.getUserId().getValue().toString();
        entity.notifyApplicationReceived = pref.isNotifyApplicationReceived();
        entity.notifyApplicationAccepted = pref.isNotifyApplicationAccepted();
        entity.notifyApplicationRejected = pref.isNotifyApplicationRejected();
        entity.notifyDmReceived = pref.isNotifyDmReceived();
        return entity;
    }

    public NotificationPreference toDomain() {
        return NotificationPreference.reconstitute(
                id,
                UserId.of(userId),
                notifyApplicationReceived,
                notifyApplicationAccepted,
                notifyApplicationRejected,
                notifyDmReceived
        );
    }
}
