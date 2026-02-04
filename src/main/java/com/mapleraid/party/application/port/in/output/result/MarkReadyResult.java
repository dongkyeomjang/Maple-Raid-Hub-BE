package com.mapleraid.party.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.party.domain.PartyRoom;
import lombok.Getter;

@Getter
public class MarkReadyResult extends SelfValidating<MarkReadyResult> {

    private final String id;
    private final boolean allReady;

    public MarkReadyResult(String id, boolean allReady) {
        this.id = id;
        this.allReady = allReady;
        this.validateSelf();
    }

    public static MarkReadyResult from(PartyRoom room) {
        return new MarkReadyResult(
                room.getId().getValue().toString(),
                room.isAllReady()
        );
    }
}
