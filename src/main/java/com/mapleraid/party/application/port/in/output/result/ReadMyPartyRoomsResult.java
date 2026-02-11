package com.mapleraid.party.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class ReadMyPartyRoomsResult extends SelfValidating<ReadMyPartyRoomsResult> {

    private final List<PartyRoomSummary> rooms;

    public ReadMyPartyRoomsResult(List<PartyRoomSummary> rooms) {
        this.rooms = rooms;
        this.validateSelf();
    }

    @Getter
    public static class PartyRoomSummary {

        private final String id;
        private final String postId;
        private final List<String> bossIds;
        private final String status;
        private final int memberCount;
        private final Instant scheduledTime;
        private final boolean scheduleConfirmed;
        private final Instant readyCheckStartedAt;
        private final boolean allReady;
        private final Instant createdAt;
        private final Instant completedAt;
        private final List<MemberSummary> members;
        private final String lastMessage;
        private final Instant lastMessageAt;

        public PartyRoomSummary(String id, String postId, List<String> bossIds, String status,
                                int memberCount, Instant scheduledTime, boolean scheduleConfirmed,
                                Instant readyCheckStartedAt, boolean allReady,
                                Instant createdAt, Instant completedAt,
                                List<MemberSummary> members,
                                String lastMessage, Instant lastMessageAt) {
            this.id = id;
            this.postId = postId;
            this.bossIds = bossIds;
            this.status = status;
            this.memberCount = memberCount;
            this.scheduledTime = scheduledTime;
            this.scheduleConfirmed = scheduleConfirmed;
            this.readyCheckStartedAt = readyCheckStartedAt;
            this.allReady = allReady;
            this.createdAt = createdAt;
            this.completedAt = completedAt;
            this.members = members;
            this.lastMessage = lastMessage;
            this.lastMessageAt = lastMessageAt;
        }
    }

    @Getter
    public static class MemberSummary {
        private final String userId;
        private final String characterId;
        private final String characterName;
        private final String characterImageUrl;
        private final String worldName;
        private final boolean isLeader;
        private final boolean isReady;
        private final Instant joinedAt;
        private final int unreadCount;

        public MemberSummary(String userId, String characterId, String characterName,
                             String characterImageUrl, String worldName, boolean isLeader, boolean isReady,
                             Instant joinedAt, int unreadCount) {
            this.userId = userId;
            this.characterId = characterId;
            this.characterName = characterName;
            this.characterImageUrl = characterImageUrl;
            this.worldName = worldName;
            this.isLeader = isLeader;
            this.isReady = isReady;
            this.joinedAt = joinedAt;
            this.unreadCount = unreadCount;
        }
    }
}
