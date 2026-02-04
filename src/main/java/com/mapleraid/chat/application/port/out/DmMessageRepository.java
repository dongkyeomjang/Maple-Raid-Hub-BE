package com.mapleraid.chat.application.port.out;

import com.mapleraid.chat.domain.DirectMessage;
import com.mapleraid.chat.domain.DirectMessageId;
import com.mapleraid.chat.domain.DirectMessageRoomId;
import com.mapleraid.user.domain.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DmMessageRepository {

    DirectMessage saveDmMessage(DirectMessage message);

    Optional<DirectMessage> findDmMessageById(DirectMessageId id);

    Page<DirectMessage> findDmMessagesByRoomId(DirectMessageRoomId roomId, Pageable pageable);

    List<DirectMessage> findDmMessagesByRoomIdAsc(DirectMessageRoomId roomId);

    DmMessagesPage findDmMessagesByRoomIdWithCursor(DirectMessageRoomId roomId, int limit, Instant before);

    int markDmAsRead(DirectMessageRoomId roomId, UserId userId);

    record DmMessagesPage(
            List<DirectMessage> messages,
            boolean hasMore,
            String nextCursor
    ) {
    }
}
