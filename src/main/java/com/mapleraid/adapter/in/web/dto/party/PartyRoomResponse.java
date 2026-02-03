package com.mapleraid.adapter.in.web.dto.party;

import com.mapleraid.domain.character.Character;
import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.party.PartyMember;
import com.mapleraid.domain.party.PartyRoom;
import com.mapleraid.domain.party.PartyRoomStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record PartyRoomResponse(
        String id,
        String postId,
        List<String> bossIds,
        PartyRoomStatus status,
        List<MemberResponse> members,
        boolean readyCheckActive,
        boolean allReady,
        Instant scheduledTime,
        boolean scheduleConfirmed,
        Instant createdAt,
        Instant completedAt,
        String lastMessage,
        Instant lastMessageAt
) {
    public static PartyRoomResponse from(PartyRoom partyRoom, Map<CharacterId, Character> characterMap) {
        return from(partyRoom, characterMap, null, null);
    }

    public static PartyRoomResponse from(PartyRoom partyRoom, Map<CharacterId, Character> characterMap,
                                         String lastMessage, Instant lastMessageAt) {
        List<MemberResponse> members = partyRoom.getMembers().stream()
                .filter(PartyMember::isActive)
                .map(m -> MemberResponse.from(m, characterMap.get(m.getCharacterId())))
                .toList();

        return new PartyRoomResponse(
                partyRoom.getId().getValue().toString(),
                partyRoom.getPostId() != null ? partyRoom.getPostId().getValue().toString() : null,
                partyRoom.getBossIds(),
                partyRoom.getStatus(),
                members,
                partyRoom.getReadyCheckStartedAt() != null,
                partyRoom.isAllReady(),
                partyRoom.getScheduledTime(),
                partyRoom.isScheduleConfirmed(),
                partyRoom.getCreatedAt(),
                partyRoom.getCompletedAt(),
                lastMessage,
                lastMessageAt
        );
    }

    public record MemberResponse(
            String userId,
            String characterId,
            String characterName,
            String characterImageUrl,
            boolean isLeader,
            boolean isReady,
            Instant joinedAt,
            int unreadCount
    ) {
        public static MemberResponse from(PartyMember member, Character character) {
            return new MemberResponse(
                    member.getUserId().getValue().toString(),
                    member.getCharacterId().getValue().toString(),
                    character != null ? character.getCharacterName() : null,
                    character != null ? character.getCharacterImageUrl() : null,
                    member.isLeader(),
                    member.isReady(),
                    member.getJoinedAt(),
                    member.getUnreadCount()
            );
        }
    }
}
