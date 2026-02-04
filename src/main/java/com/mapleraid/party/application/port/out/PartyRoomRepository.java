package com.mapleraid.party.application.port.out;

import com.mapleraid.party.domain.PartyRoom;
import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.party.domain.PartyRoomStatus;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;

import java.util.List;
import java.util.Optional;

public interface PartyRoomRepository {

    PartyRoom save(PartyRoom partyRoom);

    Optional<PartyRoom> findById(PartyRoomId id);

    Optional<PartyRoom> findByPostId(PostId postId);

    List<PartyRoom> findByMemberUserId(UserId userId);

    List<PartyRoom> findByMemberUserIdAndStatus(UserId userId, PartyRoomStatus status);
}
