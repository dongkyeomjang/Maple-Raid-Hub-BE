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

        Set<CharacterId> characterIds = partyRoom.getActiveMembers().stream()
                .map(PartyMember::getCharacterId)
                .collect(Collectors.toSet());

        Map<CharacterId, Character> characterMap = characterRepository.findByIds(characterIds).stream()
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
                lastMessageInfo != null ? lastMessageInfo.content() : null,
                lastMessageInfo != null ? lastMessageInfo.timestamp() : null
        );
    }
}
