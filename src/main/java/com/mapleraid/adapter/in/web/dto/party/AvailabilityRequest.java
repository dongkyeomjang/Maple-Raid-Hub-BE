package com.mapleraid.adapter.in.web.dto.party;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AvailabilityRequest(
        @NotNull(message = "가용 시간대 목록은 필수입니다.")
        List<TimeSlotDto> slots
) {
    public record TimeSlotDto(
            @NotNull LocalDate date,
            @NotNull LocalTime time
    ) {
    }
}
