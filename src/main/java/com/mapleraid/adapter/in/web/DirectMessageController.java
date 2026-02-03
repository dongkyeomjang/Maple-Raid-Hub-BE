package com.mapleraid.adapter.in.web;

import com.mapleraid.adapter.in.web.dto.ApiResponse;
import com.mapleraid.adapter.in.web.dto.dm.CreateDmRoomRequest;
import com.mapleraid.adapter.in.web.dto.dm.DmMessageResponse;
import com.mapleraid.adapter.in.web.dto.dm.DmRoomResponse;
import com.mapleraid.adapter.in.web.dto.dm.SendDmRequest;
import com.mapleraid.adapter.in.web.security.CurrentUser;
import com.mapleraid.adapter.in.websocket.dto.DmChatMessage;
import com.mapleraid.application.port.out.DirectMessageRoomRepository;
import com.mapleraid.application.service.DirectMessageService;
import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.chat.DirectMessage;
import com.mapleraid.domain.chat.DirectMessageRoom;
import com.mapleraid.domain.chat.DirectMessageRoomId;
import com.mapleraid.domain.post.PostId;
import com.mapleraid.domain.user.UserId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dm")
public class DirectMessageController {

    private final DirectMessageService dmService;
    private final DirectMessageRoomRepository dmRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public DirectMessageController(DirectMessageService dmService,
                                   DirectMessageRoomRepository dmRoomRepository,
                                   SimpMessagingTemplate messagingTemplate) {
        this.dmService = dmService;
        this.dmRoomRepository = dmRoomRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * DM 방 생성 또는 기존 방 조회
     */
    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<DmRoomResponse>> createOrGetRoom(
            @CurrentUser UserId userId,
            @Valid @RequestBody CreateDmRoomRequest request) {

        PostId postId = request.postId() != null ? PostId.of(request.postId()) : null;
        UserId targetUserId = UserId.of(request.targetUserId());
        CharacterId senderCharId = request.senderCharacterId() != null
                ? CharacterId.of(request.senderCharacterId()) : null;
        CharacterId targetCharId = request.targetCharacterId() != null
                ? CharacterId.of(request.targetCharacterId()) : null;

        DirectMessageRoom room = dmService.getOrCreateRoom(postId, userId, targetUserId, senderCharId, targetCharId);

        String otherNickname = dmService.getUserNickname(room.getOtherUser(userId));
        String lastContent = room.getLastMessage() != null ? room.getLastMessage().getContent() : null;

        // 상대방 캐릭터 정보 조회
        var otherCharInfo = dmService.getCharacterInfo(room.getOtherUserCharacterId(userId));
        String otherCharName = otherCharInfo != null ? otherCharInfo.name() : null;
        String otherCharImageUrl = otherCharInfo != null ? otherCharInfo.imageUrl() : null;

        // 내 캐릭터 정보 조회
        var myCharInfo = dmService.getCharacterInfo(room.getMyCharacterId(userId));
        String myCharName = myCharInfo != null ? myCharInfo.name() : null;
        String myCharImageUrl = myCharInfo != null ? myCharInfo.imageUrl() : null;

        return ResponseEntity.ok(ApiResponse.success(
                DmRoomResponse.from(room, userId, otherNickname, lastContent, otherCharName, otherCharImageUrl, myCharName, myCharImageUrl)
        ));
    }

    /**
     * 내 DM 방 목록 조회
     */
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<DmRoomResponse>>> getMyRooms(@CurrentUser UserId userId) {
        List<DirectMessageRoom> rooms = dmService.getMyRooms(userId);

        List<DmRoomResponse> responses = rooms.stream()
                .map(room -> {
                    String otherNickname = dmService.getUserNickname(room.getOtherUser(userId));
                    String lastContent = room.getLastMessage() != null ? room.getLastMessage().getContent() : null;
                    var otherCharInfo = dmService.getCharacterInfo(room.getOtherUserCharacterId(userId));
                    String otherCharName = otherCharInfo != null ? otherCharInfo.name() : null;
                    String otherCharImageUrl = otherCharInfo != null ? otherCharInfo.imageUrl() : null;
                    var myCharInfo = dmService.getCharacterInfo(room.getMyCharacterId(userId));
                    String myCharName = myCharInfo != null ? myCharInfo.name() : null;
                    String myCharImageUrl = myCharInfo != null ? myCharInfo.imageUrl() : null;
                    return DmRoomResponse.from(room, userId, otherNickname, lastContent, otherCharName, otherCharImageUrl, myCharName, myCharImageUrl);
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * DM 방 상세 조회
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<DmRoomResponse>> getRoom(
            @CurrentUser UserId userId,
            @PathVariable String roomId) {

        DirectMessageRoom room = dmService.getRoom(DirectMessageRoomId.of(roomId), userId);

        String otherNickname = dmService.getUserNickname(room.getOtherUser(userId));
        String lastContent = room.getLastMessage() != null ? room.getLastMessage().getContent() : null;
        var otherCharInfo = dmService.getCharacterInfo(room.getOtherUserCharacterId(userId));
        String otherCharName = otherCharInfo != null ? otherCharInfo.name() : null;
        String otherCharImageUrl = otherCharInfo != null ? otherCharInfo.imageUrl() : null;
        var myCharInfo = dmService.getCharacterInfo(room.getMyCharacterId(userId));
        String myCharName = myCharInfo != null ? myCharInfo.name() : null;
        String myCharImageUrl = myCharInfo != null ? myCharInfo.imageUrl() : null;

        return ResponseEntity.ok(ApiResponse.success(
                DmRoomResponse.from(room, userId, otherNickname, lastContent, otherCharName, otherCharImageUrl, myCharName, myCharImageUrl)
        ));
    }

    /**
     * 메시지 조회 (커서 기반 페이지네이션)
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<DmMessagesPageResponse>> getMessages(
            @CurrentUser UserId userId,
            @PathVariable String roomId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String before) {

        java.time.Instant beforeInstant = before != null ? java.time.Instant.parse(before) : null;

        var result = dmService.getMessagesWithCursor(
                DirectMessageRoomId.of(roomId), userId, limit, beforeInstant);

        List<DmMessageResponse> messages = result.messages().stream()
                .map(msg -> {
                    String senderNickname = msg.getSenderId() != null
                            ? dmService.getUserNickname(msg.getSenderId())
                            : "System";
                    // 캐릭터 정보 조회
                    var charInfo = dmService.getCharacterInfo(msg.getSenderCharacterId());
                    String characterName = charInfo != null ? charInfo.name() : null;
                    String characterImageUrl = charInfo != null ? charInfo.imageUrl() : null;
                    return DmMessageResponse.from(msg, senderNickname, characterName, characterImageUrl);
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                new DmMessagesPageResponse(messages, result.hasMore(), result.nextCursor())
        ));
    }

    /**
     * 메시지 전송 (REST API)
     */
    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<DmMessageResponse>> sendMessage(
            @CurrentUser UserId userId,
            @PathVariable String roomId,
            @Valid @RequestBody SendDmRequest request) {

        CharacterId senderCharId = request.senderCharacterId() != null
                ? CharacterId.of(request.senderCharacterId())
                : null;

        DirectMessageRoomId dmRoomId = DirectMessageRoomId.of(roomId);
        DirectMessage message = dmService.sendMessage(dmRoomId, userId, senderCharId, request.content());

        String senderNickname = dmService.getUserNickname(userId);

        // 캐릭터 정보 조회
        var charInfo = dmService.getCharacterInfo(senderCharId);
        String characterName = charInfo != null ? charInfo.name() : null;
        String characterImageUrl = charInfo != null ? charInfo.imageUrl() : null;

        // WebSocket으로 브로드캐스트
        DmChatMessage wsMessage = DmChatMessage.text(
                roomId,
                userId.getValue().toString(),
                senderNickname,
                senderCharId != null ? senderCharId.getValue().toString() : null,
                characterName,
                characterImageUrl,
                message.getContent()
        );
        messagingTemplate.convertAndSend("/topic/dm/" + roomId, wsMessage);

        // 상대방에게 알림 전송
        DirectMessageRoom room = dmRoomRepository.findById(dmRoomId).orElse(null);
        if (room != null) {
            UserId otherUserId = room.getOtherUser(userId);
            String displayName = characterName != null ? characterName : senderNickname;
            messagingTemplate.convertAndSendToUser(
                    otherUserId.getValue().toString(),
                    "/queue/notifications",
                    new DmNotification(roomId, displayName, "새 메시지가 도착했습니다.")
            );
        }

        return ResponseEntity.ok(ApiResponse.success(
                DmMessageResponse.from(message, senderNickname, characterName, characterImageUrl)
        ));
    }

    /**
     * 읽음 처리
     */
    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markAsRead(
            @CurrentUser UserId userId,
            @PathVariable String roomId) {

        dmService.markAsRead(DirectMessageRoomId.of(roomId), userId);
        return ResponseEntity.noContent().build();
    }

    public record DmMessagesPageResponse(
            List<DmMessageResponse> messages,
            boolean hasMore,
            String nextCursor
    ) {
    }

    public record DmNotification(String roomId, String senderNickname, String message) {
    }
}
