package com.mapleraid.adapter.in.web.dto.party;

import com.mapleraid.domain.partyroom.Availability;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AvailabilityResponse(
        String id,
        String partyRoomId,
        String userId,
        String userNickname,
        String characterName,
        List<TimeSlotDto> slots,
        Instant updatedAt
) {
    public static AvailabilityResponse from(Availability availability, String userNickname, String characterName) {
        List<TimeSlotDto> slotDtos = availability.getSlots().stream()
                .map(slot -> new TimeSlotDto(slot.date(), slot.time()))
                .toList();

        return new AvailabilityResponse(
                availability.getId().getValue().toString(),
                availability.getPartyRoomId().getValue().toString(),
                availability.getUserId().getValue().toString(),
                userNickname,
                characterName,
                slotDtos,
                availability.getUpdatedAt()
        );
    }

    public record TimeSlotDto(LocalDate date, LocalTime time) {
    }
}
