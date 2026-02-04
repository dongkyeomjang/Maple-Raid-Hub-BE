package com.mapleraid.party.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.party.domain.PartyRoom;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class CompletePartyRoomResult extends SelfValidating<CompletePartyRoomResult> {

    private final String id;
    private final String postId;
    private final List<String> bossIds;
    private final String status;
    private final Instant completedAt;

    public CompletePartyRoomResult(String id, String postId, List<String> bossIds, String status, Instant completedAt) {
        this.id = id;
        this.postId = postId;
        this.bossIds = bossIds;
        this.status = status;
        this.completedAt = completedAt;
        this.validateSelf();
    }

    public static CompletePartyRoomResult from(PartyRoom room) {
        return new CompletePartyRoomResult(
                room.getId().getValue().toString(),
                room.getPostId().getValue().toString(),
                room.getBossIds(),
                room.getStatus().name(),
                room.getCompletedAt()
        );
    }
}
