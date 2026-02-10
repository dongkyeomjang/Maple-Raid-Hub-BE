package com.mapleraid.notification.adapter.in.web.dto;

import com.mapleraid.notification.domain.NotificationPreference;

public record NotificationPreferenceResponse(
        boolean notifyApplicationReceived,
        boolean notifyApplicationAccepted,
        boolean notifyApplicationRejected,
        boolean notifyDmReceived
) {
    public static NotificationPreferenceResponse from(NotificationPreference pref) {
        return new NotificationPreferenceResponse(
                pref.isNotifyApplicationReceived(),
                pref.isNotifyApplicationAccepted(),
                pref.isNotifyApplicationRejected(),
                pref.isNotifyDmReceived()
        );
    }
}
