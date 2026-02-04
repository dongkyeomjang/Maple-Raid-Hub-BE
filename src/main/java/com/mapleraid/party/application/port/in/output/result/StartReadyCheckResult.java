package com.mapleraid.party.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.party.domain.PartyRoom;
import lombok.Getter;

import java.time.Instant;

@Getter
public class StartReadyCheckResult extends SelfValidating<StartReadyCheckResult> {

    private final String id;
    private final Instant readyCheckStartedAt;
    private final boolean allReady;

    public StartReadyCheckResult(String id, Instant readyCheckStartedAt, boolean allReady) {
        this.id = id;
        this.readyCheckStartedAt = readyCheckStartedAt;
        this.allReady = allReady;
        this.validateSelf();
    }

    public static StartReadyCheckResult from(PartyRoom room) {
        return new StartReadyCheckResult(
                room.getId().getValue().toString(),
                room.getReadyCheckStartedAt(),
                room.isAllReady()
        );
    }
}
