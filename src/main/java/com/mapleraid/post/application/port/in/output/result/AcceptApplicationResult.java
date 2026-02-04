package com.mapleraid.post.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.party.domain.PartyRoom;
import com.mapleraid.post.domain.Application;
import lombok.Getter;

@Getter
public class AcceptApplicationResult extends SelfValidating<AcceptApplicationResult> {

    private final String applicationId;
    private final String partyRoomId;

    public AcceptApplicationResult(String applicationId, String partyRoomId) {
        this.applicationId = applicationId;
        this.partyRoomId = partyRoomId;
        this.validateSelf();
    }

    public static AcceptApplicationResult from(Application application, PartyRoom partyRoom) {
        return new AcceptApplicationResult(
                application.getId().getValue().toString(),
                partyRoom != null ? partyRoom.getId().getValue().toString() : null);
    }
}
