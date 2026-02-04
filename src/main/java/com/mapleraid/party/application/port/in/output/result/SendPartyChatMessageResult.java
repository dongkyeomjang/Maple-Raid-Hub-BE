package com.mapleraid.party.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

import java.util.List;

@Getter
public class SendPartyChatMessageResult extends SelfValidating<SendPartyChatMessageResult> {

    private final String partyRoomId;
    private final String senderId;
    private final String senderNickname;
    private final String content;
    private final List<UserId> otherMemberIds;

    private SendPartyChatMessageResult(String partyRoomId, String senderId, String senderNickname,
                                       String content, List<UserId> otherMemberIds) {
        this.partyRoomId = partyRoomId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.content = content;
        this.otherMemberIds = otherMemberIds;
        this.validateSelf();
    }

    public static SendPartyChatMessageResult of(String partyRoomId, String senderId,
                                                String senderNickname, String content,
                                                List<UserId> otherMemberIds) {
        return new SendPartyChatMessageResult(partyRoomId, senderId, senderNickname, content, otherMemberIds);
    }
}
