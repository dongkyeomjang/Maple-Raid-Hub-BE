package com.mapleraid.party.application.port.in.input.command;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.user.domain.UserId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SendPartyChatMessageInput extends SelfValidating<SendPartyChatMessageInput> {

    @NotNull(message = "파티방 아이디는 필수입니다.")
    private final PartyRoomId partyRoomId;

    @NotNull(message = "발신자 아이디는 필수입니다.")
    private final UserId senderId;

    @NotBlank(message = "발신자 닉네임은 필수입니다.")
    private final String senderNickname;

    @NotBlank(message = "메시지 내용은 필수입니다.")
    @Size(max = 2000, message = "메시지는 2000자를 초과할 수 없습니다.")
    private final String content;

    private SendPartyChatMessageInput(PartyRoomId partyRoomId, UserId senderId,
                                      String senderNickname, String content) {
        this.partyRoomId = partyRoomId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.content = content;
        this.validateSelf();
    }

    public static SendPartyChatMessageInput of(PartyRoomId partyRoomId, UserId senderId,
                                               String senderNickname, String content) {
        return new SendPartyChatMessageInput(partyRoomId, senderId, senderNickname, content);
    }
}
