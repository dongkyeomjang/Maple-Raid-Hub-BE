package com.mapleraid.party.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.application.port.in.input.query.ReadPartyRoomInput;
import com.mapleraid.party.application.port.in.output.result.ReadPartyRoomResult;
import com.mapleraid.party.application.port.in.usecase.ReadPartyRoomUseCase;
import com.mapleraid.party.application.port.out.PartyChatMessageRepository;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.PartyMember;
import com.mapleraid.party.domain.PartyRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadPartyRoomService implements ReadPartyRoomUseCase {

    private final PartyRoomRepository partyRoomRepository;
    private final CharacterRepository characterRepository;
    private final PartyChatMessageRepository partyChatMessageRepository;

    @Override
    @Transactional(readOnly = true)
    public ReadPartyRoomResult execute(ReadPartyRoomInput input) {
        PartyRoom partyRoom = partyRoomRepository.findById(input.getPartyRoomId())
                .orElseThrow(() -> new CommonException(ErrorCode.PARTY_NOT_FOUND));
        if (!partyRoom.isMember(input.getRequesterId())) {
            throw new CommonException(ErrorCode.PARTY_NOT_MEMBER);
        }

        // 활성 + 탈퇴 멤버의 캐릭터를 모두 일괄 조회
        Set<CharacterId> allCharacterIds = partyRoom.getMembers().stream()
                .map(PartyMember::getCharacterId)
                .collect(Collectors.toSet());

        Map<CharacterId, Character> characterMap = characterRepository.findByIds(allCharacterIds).stream()
                .collect(Collectors.toMap(Character::getId, c -> c));

        List<ReadPartyRoomResult.MemberInfo> memberInfos = partyRoom.getActiveMembers().stream()
                .map(m -> {
                    Character character = characterMap.get(m.getCharacterId());
                    return new ReadPartyRoomResult.MemberInfo(
                            m.getUserId().getValue().toString(),
                            m.getCharacterId().getValue().toString(),
                            character != null ? character.getCharacterName() : null,
                            character != null ? character.getCharacterImageUrl() : null,
                            character != null ? character.getWorldName() : null,
                            m.isLeader(),
                            m.isReady(),
                            m.getJoinedAt(),
                            m.getUnreadCount());
                })
                .toList();

        // 탈퇴한 멤버 목록 (요청자의 합류 시점 이전에 탈퇴한 멤버는 제외)
        PartyMember requester = partyRoom.getActiveMembers().stream()
                .filter(m -> m.getUserId().equals(input.getRequesterId()))
                .findFirst()
                .orElse(null);
        Instant requesterJoinedAt = requester != null ? requester.getJoinedAt() : null;

        List<ReadPartyRoomResult.LeftMemberInfo> leftMemberInfos = partyRoom.getLeftMembers().stream()
                .filter(m -> requesterJoinedAt == null || !m.getLeftAt().isBefore(requesterJoinedAt))
                .map(m -> {
                    Character character = characterMap.get(m.getCharacterId());
                    return new ReadPartyRoomResult.LeftMemberInfo(
                            m.getUserId().getValue().toString(),
                            m.getCharacterId().getValue().toString(),
                            character != null ? character.getCharacterName() : null,
                            character != null ? character.getCharacterImageUrl() : null,
                            m.getJoinedAt(),
                            m.getLeftAt());
                })
                .toList();

        PartyChatMessageRepository.LastMessageInfo lastMessageInfo =
                partyChatMessageRepository.getLastMessageInfo(partyRoom.getId().getValue().toString());

        return new ReadPartyRoomResult(
                partyRoom.getId().getValue().toString(),
                partyRoom.getPostId().getValue().toString(),
                partyRoom.getBossIds(),
                partyRoom.getStatus().name(),
                partyRoom.getScheduledTime(),
                partyRoom.isScheduleConfirmed(),
                partyRoom.getReadyCheckStartedAt(),
                partyRoom.isAllReady(),
                partyRoom.getCreatedAt(),
                partyRoom.getCompletedAt(),
                memberInfos,
                leftMemberInfos,
                lastMessageInfo != null ? lastMessageInfo.content() : null,
                lastMessageInfo != null ? lastMessageInfo.timestamp() : null
        );
    }
}
