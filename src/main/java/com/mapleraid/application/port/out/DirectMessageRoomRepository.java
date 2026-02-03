package com.mapleraid.application.port.out;

import com.mapleraid.domain.chat.DirectMessageRoom;
import com.mapleraid.domain.chat.DirectMessageRoomId;
import com.mapleraid.domain.post.PostId;
import com.mapleraid.domain.user.UserId;

import java.util.List;
import java.util.Optional;

public interface DirectMessageRoomRepository {

    DirectMessageRoom save(DirectMessageRoom room);

    Optional<DirectMessageRoom> findById(DirectMessageRoomId id);

    List<DirectMessageRoom> findByUserId(UserId userId);

    Optional<DirectMessageRoom> findByPostIdAndUsers(PostId postId, UserId user1Id, UserId user2Id);

    Optional<DirectMessageRoom> findByUsersWithoutPost(UserId user1Id, UserId user2Id);
}
