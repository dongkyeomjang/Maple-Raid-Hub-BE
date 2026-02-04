package com.mapleraid.party.adapter.out.persistence.jpa;

import com.mapleraid.party.domain.Availability;
import com.mapleraid.party.domain.AvailabilityId;
import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.user.domain.UserId;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "availability",
        uniqueConstraints = @UniqueConstraint(columnNames = {"party_room_id", "user_id"}))
public class AvailabilityJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "party_room_id", nullable = false, length = 36)
    private String partyRoomId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @ElementCollection
    @CollectionTable(name = "availability_slots", joinColumns = @JoinColumn(name = "availability_id"))
    private List<TimeSlotEmbeddable> slots = new ArrayList<>();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static AvailabilityJpaEntity fromDomain(Availability availability) {
        AvailabilityJpaEntity entity = new AvailabilityJpaEntity();
        entity.id = availability.getId().getValue().toString();
        entity.partyRoomId = availability.getPartyRoomId().getValue().toString();
        entity.userId = availability.getUserId().getValue().toString();
        entity.slots = availability.getSlots().stream()
                .map(slot -> new TimeSlotEmbeddable(slot.date(), slot.time()))
                .toList();
        entity.updatedAt = availability.getUpdatedAt();
        return entity;
    }

    public Availability toDomain() {
        List<Availability.TimeSlot> domainSlots = slots.stream()
                .map(slot -> new Availability.TimeSlot(slot.getDate(), slot.getTime()))
                .toList();

        return Availability.reconstitute(
                AvailabilityId.of(id),
                PartyRoomId.of(partyRoomId),
                UserId.of(userId),
                domainSlots,
                updatedAt
        );
    }

    @Embeddable
    @Getter
    public static class TimeSlotEmbeddable {
        @Column(name = "slot_date")
        private LocalDate date;

        @Column(name = "slot_time")
        private LocalTime time;

        protected TimeSlotEmbeddable() {
        }

        public TimeSlotEmbeddable(LocalDate date, LocalTime time) {
            this.date = date;
            this.time = time;
        }
    }
}
