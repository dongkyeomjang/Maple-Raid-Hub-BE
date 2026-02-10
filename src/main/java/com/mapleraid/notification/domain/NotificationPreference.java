package com.mapleraid.notification.domain;

import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class NotificationPreference {

    private Long id;
    private final UserId userId;
    private boolean notifyApplicationReceived;
    private boolean notifyApplicationAccepted;
    private boolean notifyApplicationRejected;
    private boolean notifyDmReceived;

    private NotificationPreference(UserId userId) {
        this.userId = userId;
        this.notifyApplicationReceived = true;
        this.notifyApplicationAccepted = true;
        this.notifyApplicationRejected = true;
        this.notifyDmReceived = true;
    }

    public static NotificationPreference createDefault(UserId userId) {
        return new NotificationPreference(userId);
    }

    public static NotificationPreference reconstitute(
            Long id, UserId userId,
            boolean notifyApplicationReceived,
            boolean notifyApplicationAccepted,
            boolean notifyApplicationRejected,
            boolean notifyDmReceived) {
        NotificationPreference pref = new NotificationPreference(userId);
        pref.id = id;
        pref.notifyApplicationReceived = notifyApplicationReceived;
        pref.notifyApplicationAccepted = notifyApplicationAccepted;
        pref.notifyApplicationRejected = notifyApplicationRejected;
        pref.notifyDmReceived = notifyDmReceived;
        return pref;
    }

    public void update(Boolean notifyApplicationReceived, Boolean notifyApplicationAccepted,
                       Boolean notifyApplicationRejected, Boolean notifyDmReceived) {
        if (notifyApplicationReceived != null) this.notifyApplicationReceived = notifyApplicationReceived;
        if (notifyApplicationAccepted != null) this.notifyApplicationAccepted = notifyApplicationAccepted;
        if (notifyApplicationRejected != null) this.notifyApplicationRejected = notifyApplicationRejected;
        if (notifyDmReceived != null) this.notifyDmReceived = notifyDmReceived;
    }
}
