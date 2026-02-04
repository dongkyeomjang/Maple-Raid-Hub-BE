package com.mapleraid.party.adapter.in.web.dto.response;

import com.mapleraid.party.application.port.in.output.result.CompletePartyRoomResult;
import com.mapleraid.party.application.port.in.output.result.ConfirmScheduleResult;
import com.mapleraid.party.application.port.in.output.result.MarkReadyResult;
import com.mapleraid.party.application.port.in.output.result.ReadMyPartyRoomsResult;
import com.mapleraid.party.application.port.in.output.result.ReadPartyRoomResult;
import com.mapleraid.party.application.port.in.output.result.StartReadyCheckResult;

import java.time.Instant;
import java.util.List;

public record PartyRoomResponseDto(
        String id,
        String postId,
        List<String> bossIds,
        String status,
        List<MemberResponseDto> members,
        boolean readyCheckActive,
        boolean allReady,
        Instant scheduledTime,
        boolean scheduleConfirmed,
        Instant createdAt,
        Instant completedAt,
        String lastMessage,
        Instant lastMessageAt
) {
    public static PartyRoomResponseDto from(ReadPartyRoomResult result) {
        List<MemberResponseDto> members = result.getMembers().stream()
                .map(m -> new MemberResponseDto(
                        m.getUserId(), m.getCharacterId(), m.getCharacterName(),
                        m.getCharacterImageUrl(), m.isLeader(), m.isReady(),
                        m.getJoinedAt(), m.getUnreadCount()))
                .toList();
        return new PartyRoomResponseDto(
                result.getId(), result.getPostId(), result.getBossIds(), result.getStatus(),
                members,
                result.getReadyCheckStartedAt() != null,
                result.isAllReady(),
                result.getScheduledTime(),
                result.isScheduleConfirmed(),
                result.getCreatedAt(),
                result.getCompletedAt(),
                result.getLastMessage(),
                result.getLastMessageAt());
    }

    public static PartyRoomResponseDto from(ReadMyPartyRoomsResult.PartyRoomSummary summary) {
        List<MemberResponseDto> members = summary.getMembers().stream()
                .map(m -> new MemberResponseDto(
                        m.getUserId(), m.getCharacterId(), m.getCharacterName(),
                        m.getCharacterImageUrl(), m.isLeader(), m.isReady(),
                        m.getJoinedAt(), m.getUnreadCount()))
                .toList();
        return new PartyRoomResponseDto(
                summary.getId(), summary.getPostId(), summary.getBossIds(), summary.getStatus(),
                members,
                summary.getReadyCheckStartedAt() != null,
                summary.isAllReady(),
                summary.getScheduledTime(),
                summary.isScheduleConfirmed(),
                summary.getCreatedAt(),
                summary.getCompletedAt(),
                summary.getLastMessage(),
                summary.getLastMessageAt());
    }

    public static PartyRoomResponseDto from(CompletePartyRoomResult result) {
        return new PartyRoomResponseDto(
                result.getId(), result.getPostId(), result.getBossIds(), result.getStatus(),
                null,
                false,
                false,
                null,
                false,
                null,
                result.getCompletedAt(),
                null,
                null);
    }

    public static PartyRoomResponseDto from(StartReadyCheckResult result) {
        return new PartyRoomResponseDto(
                result.getId(), null, null, null,
                null,
                result.getReadyCheckStartedAt() != null,
                result.isAllReady(),
                null,
                false,
                null,
                null,
                null,
                null);
    }

    public static PartyRoomResponseDto from(MarkReadyResult result) {
        return new PartyRoomResponseDto(
                result.getId(), null, null, null,
                null,
                false,
                result.isAllReady(),
                null,
                false,
                null,
                null,
                null,
                null);
    }

    public static PartyRoomResponseDto from(ConfirmScheduleResult result) {
        return new PartyRoomResponseDto(
                result.getId(), null, null, null,
                null,
                false,
                false,
                result.getScheduledTime(),
                result.isScheduleConfirmed(),
                null,
                null,
                null,
                null);
    }

    public record MemberResponseDto(
            String userId,
            String characterId,
            String characterName,
            String characterImageUrl,
            boolean isLeader,
            boolean isReady,
            Instant joinedAt,
            int unreadCount
    ) {
    }
}
