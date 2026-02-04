package com.mapleraid.party.adapter.in.web.dto.response;

import com.mapleraid.party.application.port.in.output.result.SaveAvailabilityResult;

import java.time.Instant;

public record AvailabilityResponseDto(
        String id,
        String partyRoomId,
        String userId,
        Instant updatedAt
) {
    public static AvailabilityResponseDto from(SaveAvailabilityResult result) {
        return new AvailabilityResponseDto(result.getId(), result.getPartyRoomId(), result.getUserId(), result.getUpdatedAt());
    }
}
