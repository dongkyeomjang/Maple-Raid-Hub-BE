package com.mapleraid.chat.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.chat.application.port.in.input.query.ReadMyDmRoomsInput;
import com.mapleraid.chat.application.port.in.output.result.ReadMyDmRoomsResult;
import com.mapleraid.chat.application.port.in.usecase.ReadMyDmRoomsUseCase;
import com.mapleraid.chat.application.port.out.DirectMessageRoomRepository;
import com.mapleraid.chat.domain.DirectMessageRoom;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadMyDmRoomsService implements ReadMyDmRoomsUseCase {

    private final DirectMessageRoomRepository roomRepository;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;

    @Override
    @Transactional(readOnly = true)
    public ReadMyDmRoomsResult execute(ReadMyDmRoomsInput input) {
        UserId userId = input.getUserId();
        List<DirectMessageRoom> rooms = roomRepository.findByUserId(userId);

        // 배치 조회를 위한 ID 수집
        List<UserId> otherUserIds = rooms.stream()
                .map(room -> room.getOtherUser(userId))
                .distinct()
                .toList();

        Set<CharacterId> characterIds = new HashSet<>();
        for (DirectMessageRoom room : rooms) {
            CharacterId otherCharId = room.getOtherUserCharacterId(userId);
            if (otherCharId != null) characterIds.add(otherCharId);
            CharacterId myCharId = room.getMyCharacterId(userId);
            if (myCharId != null) characterIds.add(myCharId);
        }

        // 배치 조회
        Map<UserId, User> userMap = otherUserIds.isEmpty()
                ? Collections.emptyMap()
                : userRepository.findAllByIds(otherUserIds);
        Map<CharacterId, Character> characterMap = characterIds.isEmpty()
                ? Collections.emptyMap()
                : characterRepository.findByIds(characterIds).stream()
                .collect(Collectors.toMap(Character::getId, c -> c));

        List<ReadMyDmRoomsResult.DmRoomSummary> summaries = rooms.stream()
                .map(room -> {
                    UserId otherUserId = room.getOtherUser(userId);
                    CharacterId otherCharId = room.getOtherUserCharacterId(userId);
                    CharacterId myCharId = room.getMyCharacterId(userId);

                    User otherUser = userMap.get(otherUserId);
                    Character otherChar = otherCharId != null ? characterMap.get(otherCharId) : null;
                    Character myChar = myCharId != null ? characterMap.get(myCharId) : null;

                    return new ReadMyDmRoomsResult.DmRoomSummary(
                            room.getId().getValue().toString(),
                            room.getPostId() != null ? room.getPostId().getValue().toString() : null,
                            otherUserId.getValue().toString(),
                            otherUser != null ? otherUser.getNickname() : null,
                            otherCharId != null ? otherCharId.getValue().toString() : null,
                            otherChar != null ? otherChar.getCharacterName() : null,
                            otherChar != null ? otherChar.getCharacterImageUrl() : null,
                            myCharId != null ? myCharId.getValue().toString() : null,
                            myChar != null ? myChar.getCharacterName() : null,
                            myChar != null ? myChar.getCharacterImageUrl() : null,
                            room.getUnreadCount(userId),
                            room.getLastMessage() != null ? room.getLastMessage().getContent() : null,
                            room.getLastMessageAt(),
                            room.getCreatedAt()
                    );
                })
                .toList();

        return new ReadMyDmRoomsResult(summaries);
    }
}
