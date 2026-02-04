package com.mapleraid.chat.adapter.in.web.command;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.chat.adapter.in.web.dto.request.CreateDmRoomRequestDto;
import com.mapleraid.chat.adapter.in.web.dto.request.SendDmRequestDto;
import com.mapleraid.chat.adapter.in.web.dto.response.DmMessageResponseDto;
import com.mapleraid.chat.adapter.in.web.dto.response.DmRoomResponseDto;
import com.mapleraid.chat.application.port.in.input.command.CreateDmRoomInput;
import com.mapleraid.chat.application.port.in.input.command.MarkDmAsReadInput;
import com.mapleraid.chat.application.port.in.input.command.SendDmMessageInput;
import com.mapleraid.chat.application.port.in.output.result.CreateDmRoomResult;
import com.mapleraid.chat.application.port.in.output.result.SendDmMessageResult;
import com.mapleraid.chat.application.port.in.usecase.CreateDmRoomUseCase;
import com.mapleraid.chat.application.port.in.usecase.MarkDmAsReadUseCase;
import com.mapleraid.chat.application.port.in.usecase.SendDmMessageUseCase;
import com.mapleraid.chat.domain.DirectMessageRoomId;
import com.mapleraid.core.annotation.bean.CurrentUser;
import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dm")
@RequiredArgsConstructor
public class DmCommandController {

    private final CreateDmRoomUseCase createDmRoomUseCase;
    private final SendDmMessageUseCase sendDmMessageUseCase;
    private final MarkDmAsReadUseCase markDmAsReadUseCase;

    /**
     * DM 방 생성 또는 조회하기
     */
    @PostMapping("/rooms")
    public ResponseDto<DmRoomResponseDto> createOrGetRoom(
            @CurrentUser UserId userId,
            @RequestBody CreateDmRoomRequestDto request) {

        CreateDmRoomResult result = createDmRoomUseCase.execute(CreateDmRoomInput.of(
                request.postId() != null ? PostId.of(request.postId()) : null,
                userId,
                UserId.of(request.targetUserId()),
                request.senderCharacterId() != null ? CharacterId.of(request.senderCharacterId()) : null,
                request.targetCharacterId() != null ? CharacterId.of(request.targetCharacterId()) : null
        ));
        return ResponseDto.ok(DmRoomResponseDto.from(result));
    }

    /**
     * DM 메시지 보내기
     */
    @PostMapping("/rooms/{roomId}/messages")
    public ResponseDto<DmMessageResponseDto> sendMessage(
            @CurrentUser UserId userId,
            @PathVariable String roomId,
            @RequestBody SendDmRequestDto request) {

        SendDmMessageResult result = sendDmMessageUseCase.execute(SendDmMessageInput.of(
                DirectMessageRoomId.of(roomId),
                userId,
                request.senderCharacterId() != null ? CharacterId.of(request.senderCharacterId()) : null,
                request.content()
        ));
        return ResponseDto.ok(DmMessageResponseDto.from(result));
    }

    /**
     * DM 읽음 처리하기
     */
    @PostMapping("/rooms/{roomId}/read")
    public ResponseDto<Void> markAsRead(
            @CurrentUser UserId userId,
            @PathVariable String roomId) {

        markDmAsReadUseCase.execute(MarkDmAsReadInput.of(DirectMessageRoomId.of(roomId), userId));
        return ResponseDto.ok(null);
    }
}
