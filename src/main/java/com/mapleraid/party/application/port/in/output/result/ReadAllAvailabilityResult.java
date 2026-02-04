package com.mapleraid.party.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class ReadAllAvailabilityResult extends SelfValidating<ReadAllAvailabilityResult> {

    private final List<MemberAvailability> memberAvailabilities;
    private final List<HeatmapSlot> heatmap;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public ReadAllAvailabilityResult(List<MemberAvailability> memberAvailabilities, List<HeatmapSlot> heatmap,
                                     LocalDate startDate, LocalDate endDate) {
        this.memberAvailabilities = memberAvailabilities;
        this.heatmap = heatmap;
        this.startDate = startDate;
        this.endDate = endDate;
        this.validateSelf();
    }

    @Getter
    public static class MemberAvailability {

        private final String id;
        private final String partyRoomId;
        private final String userId;
        private final String userNickname;
        private final String characterName;
        private final List<TimeSlotDto> slots;
        private final Instant updatedAt;

        public MemberAvailability(String id, String partyRoomId, String userId, String userNickname,
                                  String characterName, List<TimeSlotDto> slots, Instant updatedAt) {
            this.id = id;
            this.partyRoomId = partyRoomId;
            this.userId = userId;
            this.userNickname = userNickname;
            this.characterName = characterName;
            this.slots = slots;
            this.updatedAt = updatedAt;
        }
    }

    @Getter
    public static class TimeSlotDto {

        private final LocalDate date;
        private final LocalTime time;

        public TimeSlotDto(LocalDate date, LocalTime time) {
            this.date = date;
            this.time = time;
        }
    }

    @Getter
    public static class HeatmapSlot {

        private final LocalDate date;
        private final LocalTime time;
        private final int availableCount;
        private final List<String> userIds;

        public HeatmapSlot(LocalDate date, LocalTime time, int availableCount, List<String> userIds) {
            this.date = date;
            this.time = time;
            this.availableCount = availableCount;
            this.userIds = userIds;
        }
    }
}
