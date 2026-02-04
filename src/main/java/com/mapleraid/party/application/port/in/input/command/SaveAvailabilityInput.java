package com.mapleraid.party.application.port.in.input.command;

import com.mapleraid.party.domain.Availability;
import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

import java.util.List;

@Getter
public class SaveAvailabilityInput {

    private final PartyRoomId partyRoomId;
    private final UserId userId;
    private final List<Availability.TimeSlot> slots;

    private SaveAvailabilityInput(PartyRoomId partyRoomId, UserId userId, List<Availability.TimeSlot> slots) {
        this.partyRoomId = partyRoomId;
        this.userId = userId;
        this.slots = slots;
    }

    public static SaveAvailabilityInput of(PartyRoomId partyRoomId, UserId userId, List<Availability.TimeSlot> slots) {
        return new SaveAvailabilityInput(partyRoomId, userId, slots);
    }
}
