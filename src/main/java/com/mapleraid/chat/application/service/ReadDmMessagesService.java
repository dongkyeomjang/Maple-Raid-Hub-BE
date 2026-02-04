package com.mapleraid.chat.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.chat.application.port.in.input.query.ReadDmMessagesInput;
import com.mapleraid.chat.application.port.in.output.result.ReadDmMessagesResult;
import com.mapleraid.chat.application.port.in.usecase.ReadDmMessagesUseCase;
import com.mapleraid.chat.application.port.out.DirectMessageRoomRepository;
import com.mapleraid.chat.application.port.out.DmMessageRepository;
import com.mapleraid.chat.domain.DirectMessageRoom;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import com.mapleraid.user.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReadDmMessagesService implements ReadDmMessagesUseCase {

    private final DirectMessageRoomRepository roomRepository;
    private final DmMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;

    public ReadDmMessagesService(DirectMessageRoomRepository roomRepository,
                                 DmMessageRepository messageRepository,
                                 UserRepository userRepository,
                                 CharacterRepository characterRepository) {
        this.roomRepository = roomRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.characterRepository = characterRepository;
    }

    @Override
    public ReadDmMessagesResult execute(ReadDmMessagesInput input) {
        DirectMessageRoom room = roomRepository.findById(input.getRoomId())
                .orElseThrow(() -> new CommonException(ErrorCode.DM_ROOM_NOT_FOUND));

        if (!room.isParticipant(input.getRequesterId())) {
            throw new CommonException(ErrorCode.DM_NOT_PARTICIPANT);
        }

        DmMessageRepository.DmMessagesPage page = messageRepository.findDmMessagesByRoomIdWithCursor(
                input.getRoomId(), input.getLimit(), input.getBefore());

        List<ReadDmMessagesResult.DmMessageItem> items = page.messages().stream()
                .map(ReadDmMessagesResult.DmMessageItem::from)
                .toList();

        // Batch fetch sender user info
        List<UserId> senderUserIds = items.stream()
                .map(ReadDmMessagesResult.DmMessageItem::getSenderId)
                .filter(Objects::nonNull)
                .distinct()
                .map(UserId::of)
                .toList();
        Map<UserId, User> userMap = userRepository.findAllByIds(senderUserIds);

        // Batch fetch sender character info
        Set<CharacterId> senderCharacterIds = items.stream()
                .map(ReadDmMessagesResult.DmMessageItem::getSenderCharacterId)
                .filter(Objects::nonNull)
                .distinct()
                .map(CharacterId::of)
                .collect(Collectors.toSet());
        Map<CharacterId, Character> characterMap = characterRepository.findByIds(senderCharacterIds).stream()
                .collect(Collectors.toMap(Character::getId, Function.identity()));

        // Enrich items with sender info
        List<ReadDmMessagesResult.DmMessageItem> enrichedItems = items.stream()
                .map(item -> {
                    String nickname = null;
                    String characterName = null;
                    String characterImageUrl = null;

                    if (item.getSenderId() != null) {
                        User user = userMap.get(UserId.of(item.getSenderId()));
                        if (user != null) {
                            nickname = user.getNickname();
                        }
                    }

                    if (item.getSenderCharacterId() != null) {
                        Character character = characterMap.get(CharacterId.of(item.getSenderCharacterId()));
                        if (character != null) {
                            characterName = character.getCharacterName();
                            characterImageUrl = character.getCharacterImageUrl();
                        }
                    }

                    return item.withSenderInfo(nickname, characterName, characterImageUrl);
                })
                .toList();

        return new ReadDmMessagesResult(enrichedItems, page.hasMore(), page.nextCursor());
    }
}
