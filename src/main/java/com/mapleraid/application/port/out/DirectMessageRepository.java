package com.mapleraid.application.port.out;

import com.mapleraid.domain.chat.DirectMessage;
import com.mapleraid.domain.chat.DirectMessageId;
import com.mapleraid.domain.chat.DirectMessageRoomId;
import com.mapleraid.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface DirectMessageRepository {

    DirectMessage save(DirectMessage message);

    Optional<DirectMessage> findById(DirectMessageId id);

    Page<DirectMessage> findByRoomId(DirectMessageRoomId roomId, Pageable pageable);

    List<DirectMessage> findByRoomIdOrderByCreatedAtAsc(DirectMessageRoomId roomId);

    int markAsReadByRoomIdAndNotSender(DirectMessageRoomId roomId, UserId userId);
}
