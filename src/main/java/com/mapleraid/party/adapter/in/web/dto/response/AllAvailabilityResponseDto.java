package com.mapleraid.party.adapter.in.web.dto.response;

import com.mapleraid.party.application.port.in.output.result.ReadAllAvailabilityResult;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AllAvailabilityResponseDto(
        List<MemberAvailabilityDto> memberAvailabilities,
        List<HeatmapSlotDto> heatmap,
        LocalDate startDate,
        LocalDate endDate
) {
    public static AllAvailabilityResponseDto from(ReadAllAvailabilityResult result) {
        List<MemberAvailabilityDto> members = result.getMemberAvailabilities().stream()
                .map(m -> new MemberAvailabilityDto(m.getId(), m.getPartyRoomId(), m.getUserId(), m.getUserNickname(),
                        m.getCharacterName(),
                        m.getSlots().stream().map(s -> new TimeSlotDto(s.getDate(), s.getTime())).toList(),
                        m.getUpdatedAt()))
                .toList();
        List<HeatmapSlotDto> heatmap = result.getHeatmap().stream()
                .map(h -> new HeatmapSlotDto(h.getDate(), h.getTime(), h.getAvailableCount(), h.getUserIds()))
                .toList();
        return new AllAvailabilityResponseDto(members, heatmap, result.getStartDate(), result.getEndDate());
    }

    public record MemberAvailabilityDto(String id, String partyRoomId, String userId, String userNickname,
                                        String characterName, List<TimeSlotDto> slots, Instant updatedAt) {
    }

    public record TimeSlotDto(LocalDate date, LocalTime time) {
    }

    public record HeatmapSlotDto(LocalDate date, LocalTime time, int availableCount, List<String> userIds) {
    }
}
