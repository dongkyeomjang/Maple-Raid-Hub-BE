package com.mapleraid.party.adapter.in.web.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AvailabilityRequestDto(List<TimeSlotDto> slots) {
    public record TimeSlotDto(LocalDate date, LocalTime time) {
    }
}
