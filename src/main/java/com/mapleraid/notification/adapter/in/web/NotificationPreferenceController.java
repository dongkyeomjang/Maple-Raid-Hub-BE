package com.mapleraid.notification.adapter.in.web;

import com.mapleraid.core.annotation.bean.CurrentUser;
import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.notification.adapter.in.web.dto.NotificationPreferenceResponse;
import com.mapleraid.notification.adapter.in.web.dto.UpdateNotificationPreferenceRequest;
import com.mapleraid.notification.application.port.in.usecase.ReadNotificationPreferenceUseCase;
import com.mapleraid.notification.application.port.in.usecase.UpdateNotificationPreferenceUseCase;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final ReadNotificationPreferenceUseCase readUseCase;
    private final UpdateNotificationPreferenceUseCase updateUseCase;

    @GetMapping("/preferences")
    public ResponseDto<NotificationPreferenceResponse> getPreferences(@CurrentUser UserId userId) {
        return ResponseDto.ok(
                NotificationPreferenceResponse.from(readUseCase.execute(userId))
        );
    }

    @PatchMapping("/preferences")
    public ResponseDto<NotificationPreferenceResponse> updatePreferences(
            @CurrentUser UserId userId,
            @RequestBody UpdateNotificationPreferenceRequest request) {
        return ResponseDto.ok(
                NotificationPreferenceResponse.from(
                        updateUseCase.execute(
                                userId,
                                request.notifyApplicationReceived(),
                                request.notifyApplicationAccepted(),
                                request.notifyApplicationRejected(),
                                request.notifyDmReceived()
                        )
                )
        );
    }
}
