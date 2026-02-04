package com.mapleraid.party.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.party.domain.PartyRoom;
import lombok.Getter;

import java.time.Instant;

@Getter
public class ConfirmScheduleResult extends SelfValidating<ConfirmScheduleResult> {

    private final String id;
    private final Instant scheduledTime;
    private final boolean scheduleConfirmed;

    public ConfirmScheduleResult(String id, Instant scheduledTime, boolean scheduleConfirmed) {
        this.id = id;
        this.scheduledTime = scheduledTime;
        this.scheduleConfirmed = scheduleConfirmed;
        this.validateSelf();
    }

    public static ConfirmScheduleResult from(PartyRoom room) {
        return new ConfirmScheduleResult(
                room.getId().getValue().toString(),
                room.getScheduledTime(),
                room.isScheduleConfirmed()
        );
    }
}
