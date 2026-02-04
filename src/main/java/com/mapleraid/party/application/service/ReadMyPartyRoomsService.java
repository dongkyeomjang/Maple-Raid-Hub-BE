package com.mapleraid.party.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.party.application.port.in.input.query.ReadMyPartyRoomsInput;
import com.mapleraid.party.application.port.in.output.result.ReadMyPartyRoomsResult;
import com.mapleraid.party.application.port.in.usecase.ReadMyPartyRoomsUseCase;
import com.mapleraid.party.application.port.out.PartyChatMessageRepository;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.PartyMember;
import com.mapleraid.party.domain.PartyRoom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReadMyPartyRoomsService implements ReadMyPartyRoomsUseCase {

    private final PartyRoomRepository partyRoomRepository;
    private final CharacterRepository characterRepository;
    private final PartyChatMessageRepository partyChatMessageRepository;

    public ReadMyPartyRoomsService(PartyRoomRepository partyRoomRepository,
                                   CharacterRepository characterRepository,
                                   PartyChatMessageRepository partyChatMessageRepository) {
        this.partyRoomRepository = partyRoomRepository;
        this.characterRepository = characterRepository;
        this.partyChatMessageRepository = partyChatMessageRepository;
    }

    @Override
    public ReadMyPartyRoomsResult execute(ReadMyPartyRoomsInput input) {
        List<PartyRoom> rooms;
        if (input.getStatus() != null) {
            rooms = partyRoomRepository.findByMemberUserIdAndStatus(input.getUserId(), input.getStatus());
        } else {
            rooms = partyRoomRepository.findByMemberUserId(input.getUserId());
        }

        // 모든 활성 멤버의 characterId를 수집하여 일괄 조회
        Set<CharacterId> allCharacterIds = rooms.stream()
                .flatMap(room -> room.getActiveMembers().stream())
                .map(PartyMember::getCharacterId)
                .collect(Collectors.toSet());

        Map<CharacterId, Character> characterMap = characterRepository.findByIds(allCharacterIds).stream()
                .collect(Collectors.toMap(Character::getId, c -> c));

        List<ReadMyPartyRoomsResult.PartyRoomSummary> summaries = rooms.stream()
                .map(room -> {
                    List<ReadMyPartyRoomsResult.MemberSummary> memberSummaries = room.getActiveMembers().stream()
                            .map(m -> {
                                Character character = characterMap.get(m.getCharacterId());
                                return new ReadMyPartyRoomsResult.MemberSummary(
                                        m.getUserId().getValue().toString(),
                                        m.getCharacterId().getValue().toString(),
                                        character != null ? character.getCharacterName() : null,
                                        character != null ? character.getCharacterImageUrl() : null,
                                        m.isLeader(),
                                        m.isReady(),
                                        m.getJoinedAt(),
                                        m.getUnreadCount()
                                );
                            })
                            .toList();

                    PartyChatMessageRepository.LastMessageInfo lastMessageInfo =
                            partyChatMessageRepository.getLastMessageInfo(room.getId().getValue().toString());

                    return new ReadMyPartyRoomsResult.PartyRoomSummary(
                            room.getId().getValue().toString(),
                            room.getPostId().getValue().toString(),
                            room.getBossIds(),
                            room.getStatus().name(),
                            room.getMemberCount(),
                            room.getScheduledTime(),
                            room.isScheduleConfirmed(),
                            room.getReadyCheckStartedAt(),
                            room.isAllReady(),
                            room.getCreatedAt(),
                            room.getCompletedAt(),
                            memberSummaries,
                            lastMessageInfo != null ? lastMessageInfo.content() : null,
                            lastMessageInfo != null ? lastMessageInfo.timestamp() : null
                    );
                })
                .toList();

        return new ReadMyPartyRoomsResult(summaries);
    }
}
