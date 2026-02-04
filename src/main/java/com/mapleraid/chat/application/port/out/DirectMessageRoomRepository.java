package com.mapleraid.chat.application.port.out;

import com.mapleraid.chat.domain.DirectMessageRoom;
import com.mapleraid.chat.domain.DirectMessageRoomId;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;

import java.util.List;
import java.util.Optional;

public interface DirectMessageRoomRepository {

    DirectMessageRoom save(DirectMessageRoom room);

    Optional<DirectMessageRoom> findById(DirectMessageRoomId id);

    List<DirectMessageRoom> findByUserId(UserId userId);

    Optional<DirectMessageRoom> findByPostIdAndUsers(PostId postId, UserId user1Id, UserId user2Id);

    Optional<DirectMessageRoom> findByUsersWithoutPost(UserId user1Id, UserId user2Id);
}
