package com.mapleraid.domain.partyroom;

import com.mapleraid.domain.party.PartyRoomId;
import com.mapleraid.domain.user.UserId;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * 가용시간 엔티티 - When2Meet 스타일
 * 한 명의 유저가 특정 파티룸에 대해 가능한 시간대 정보
 */
public class Availability {

    private final AvailabilityId id;
    private final PartyRoomId partyRoomId;
    private final UserId userId;
    private List<TimeSlot> slots;  // 선택된 시간대 목록
    private Instant updatedAt;

    private Availability(AvailabilityId id, PartyRoomId partyRoomId, UserId userId) {
        this.id = Objects.requireNonNull(id);
        this.partyRoomId = Objects.requireNonNull(partyRoomId);
        this.userId = Objects.requireNonNull(userId);
        this.slots = List.of();
        this.updatedAt = Instant.now();
    }

    public static Availability create(PartyRoomId partyRoomId, UserId userId, List<TimeSlot> slots) {
        Availability availability = new Availability(AvailabilityId.generate(), partyRoomId, userId);
        availability.slots = slots != null ? List.copyOf(slots) : List.of();
        return availability;
    }

    public static Availability reconstitute(
            AvailabilityId id, PartyRoomId partyRoomId, UserId userId,
            List<TimeSlot> slots, Instant updatedAt) {
        Availability availability = new Availability(id, partyRoomId, userId);
        availability.slots = slots != null ? List.copyOf(slots) : List.of();
        availability.updatedAt = updatedAt;
        return availability;
    }

    public void updateSlots(List<TimeSlot> newSlots) {
        this.slots = newSlots != null ? List.copyOf(newSlots) : List.of();
        this.updatedAt = Instant.now();
    }

    /**
     * 특정 시간대에 가능한지 확인
     */
    public boolean isAvailableAt(LocalDate date, LocalTime time) {
        return slots.stream()
                .anyMatch(slot -> slot.date().equals(date) && slot.time().equals(time));
    }

    // Getters
    public AvailabilityId getId() {
        return id;
    }

    public PartyRoomId getPartyRoomId() {
        return partyRoomId;
    }

    public UserId getUserId() {
        return userId;
    }

    public List<TimeSlot> getSlots() {
        return slots;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 시간대 값 객체 (30분 단위)
     */
    public record TimeSlot(LocalDate date, LocalTime time) {
        public TimeSlot {
            Objects.requireNonNull(date);
            Objects.requireNonNull(time);
            // 30분 단위로 정규화
            int minute = time.getMinute() >= 30 ? 30 : 0;
            time = LocalTime.of(time.getHour(), minute);
        }
    }
}
