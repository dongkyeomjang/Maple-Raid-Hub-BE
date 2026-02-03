package com.mapleraid.adapter.in.web.dto.party;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ConfirmScheduleRequest(
        @NotNull(message = "확정 시간은 필수입니다.")
        Instant scheduledTime
) {
}
