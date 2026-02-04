package com.mapleraid.party.adapter.in.web.dto.request;

import java.time.Instant;

public record ConfirmScheduleRequestDto(Instant scheduledTime) {
}
