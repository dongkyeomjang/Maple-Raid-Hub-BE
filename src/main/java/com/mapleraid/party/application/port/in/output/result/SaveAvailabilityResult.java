package com.mapleraid.party.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.party.domain.Availability;
import lombok.Getter;

import java.time.Instant;

@Getter
public class SaveAvailabilityResult extends SelfValidating<SaveAvailabilityResult> {

    private final String id;
    private final String partyRoomId;
    private final String userId;
    private final Instant updatedAt;

    public SaveAvailabilityResult(String id, String partyRoomId, String userId, Instant updatedAt) {
        this.id = id;
        this.partyRoomId = partyRoomId;
        this.userId = userId;
        this.updatedAt = updatedAt;
        this.validateSelf();
    }

    public static SaveAvailabilityResult from(Availability a) {
        return new SaveAvailabilityResult(
                a.getId().getValue().toString(),
                a.getPartyRoomId().getValue().toString(),
                a.getUserId().getValue().toString(),
                a.getUpdatedAt()
        );
    }
}
