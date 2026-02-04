package com.mapleraid.chat.application.port.in.input.command;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.chat.domain.DirectMessageRoomId;
import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.user.domain.UserId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SendDmMessageInput extends SelfValidating<SendDmMessageInput> {

    @NotNull(message = "채팅방 아이디는 필수입니다.")
    private final DirectMessageRoomId roomId;

    @NotNull(message = "발신자 아이디는 필수입니다.")
    private final UserId senderId;

    private final CharacterId senderCharacterId;

    @NotBlank(message = "메시지 내용은 필수입니다.")
    @Size(max = 2000, message = "메시지는 2000자를 초과할 수 없습니다.")
    private final String content;

    private SendDmMessageInput(DirectMessageRoomId roomId, UserId senderId,
                               CharacterId senderCharacterId, String content) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderCharacterId = senderCharacterId;
        this.content = content;
        this.validateSelf();
    }

    public static SendDmMessageInput of(DirectMessageRoomId roomId, UserId senderId,
                                        CharacterId senderCharacterId, String content) {
        return new SendDmMessageInput(roomId, senderId, senderCharacterId, content);
    }
}
