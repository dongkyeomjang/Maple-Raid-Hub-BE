package com.mapleraid.application.port.out;

import com.mapleraid.domain.party.PartyRoom;
import com.mapleraid.domain.party.PartyRoomId;
import com.mapleraid.domain.party.PartyRoomStatus;
import com.mapleraid.domain.post.PostId;
import com.mapleraid.domain.user.UserId;

import java.util.List;
import java.util.Optional;

public interface PartyRoomRepository {

    PartyRoom save(PartyRoom partyRoom);

    Optional<PartyRoom> findById(PartyRoomId id);

    Optional<PartyRoom> findByPostId(PostId postId);

    List<PartyRoom> findByMemberUserId(UserId userId);

    List<PartyRoom> findByMemberUserIdAndStatus(UserId userId, PartyRoomStatus status);
}
