package com.mapleraid.notification.adapter.in.web.dto;

public record UpdateNotificationPreferenceRequest(
        Boolean notifyApplicationReceived,
        Boolean notifyApplicationAccepted,
        Boolean notifyApplicationRejected,
        Boolean notifyDmReceived
) {}
