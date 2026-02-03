package com.mapleraid.adapter.in.web;

import com.mapleraid.adapter.in.web.dto.ApiResponse;
import com.mapleraid.adapter.in.web.dto.party.AllAvailabilityResponse;
import com.mapleraid.adapter.in.web.dto.party.AvailabilityRequest;
import com.mapleraid.adapter.in.web.dto.party.AvailabilityResponse;
import com.mapleraid.adapter.in.web.dto.party.ConfirmScheduleRequest;
import com.mapleraid.adapter.in.web.dto.party.PartyRoomResponse;
import com.mapleraid.adapter.in.web.dto.party.ReviewRequest;
import com.mapleraid.adapter.in.web.dto.party.ReviewResponse;
import com.mapleraid.adapter.in.web.security.CurrentUser;
import com.mapleraid.application.port.out.CharacterRepository;
import com.mapleraid.application.port.out.ChatMessageRepository;
import com.mapleraid.application.service.AvailabilityService;
import com.mapleraid.application.service.PartyRoomService;
import com.mapleraid.domain.character.Character;
import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.party.PartyMember;
import com.mapleraid.domain.party.PartyRoom;
import com.mapleraid.domain.party.PartyRoomId;
import com.mapleraid.domain.party.PartyRoomStatus;
import com.mapleraid.domain.partyroom.Availability;
import com.mapleraid.domain.review.Review;
import com.mapleraid.domain.user.UserId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/party-rooms")
public class PartyRoomController {

    private final PartyRoomService partyRoomService;
    private final AvailabilityService availabilityService;
    private final CharacterRepository characterRepository;
    private final ChatMessageRepository chatMessageRepository;

    public PartyRoomController(PartyRoomService partyRoomService,
                               AvailabilityService availabilityService,
                               CharacterRepository characterRepository,
                               ChatMessageRepository chatMessageRepository) {
        this.partyRoomService = partyRoomService;
        this.availabilityService = availabilityService;
        this.characterRepository = characterRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    private Map<CharacterId, Character> getCharacterMap(PartyRoom partyRoom) {
        Set<CharacterId> characterIds = partyRoom.getMembers().stream()
                .filter(PartyMember::isActive)
                .map(PartyMember::getCharacterId)
                .collect(Collectors.toSet());
        return characterRepository.findByIds(characterIds).stream()
                .collect(Collectors.toMap(Character::getId, Function.identity()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PartyRoomResponse>>> getMyPartyRooms(
            @CurrentUser UserId userId,
            @RequestParam(required = false) PartyRoomStatus status) {

        List<PartyRoom> partyRooms = partyRoomService.getMyPartyRooms(userId, status);

        List<PartyRoomResponse> responses = partyRooms.stream()
                .map(room -> {
                    // 마지막 메시지 정보 조회
                    ChatMessageRepository.LastMessageInfo lastMsgInfo =
                            chatMessageRepository.getLastMessageInfo(room.getId().getValue().toString());
                    String lastMessage = lastMsgInfo != null ? lastMsgInfo.content() : null;
                    java.time.Instant lastMessageAt = lastMsgInfo != null ? lastMsgInfo.timestamp() : null;
                    return PartyRoomResponse.from(room, getCharacterMap(room), lastMessage, lastMessageAt);
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{partyRoomId}")
    public ResponseEntity<ApiResponse<PartyRoomResponse>> getPartyRoom(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId) {

        PartyRoom partyRoom = partyRoomService.getPartyRoom(
                PartyRoomId.of(partyRoomId),
                userId
        );

        return ResponseEntity.ok(ApiResponse.success(PartyRoomResponse.from(partyRoom, getCharacterMap(partyRoom))));
    }

    @GetMapping("/{partyRoomId}/messages")
    public ResponseEntity<ApiResponse<PartyChatMessagesPageResponse>> getMessages(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String before) {

        // 권한 확인 - 파티 멤버인지
        partyRoomService.getPartyRoom(
                PartyRoomId.of(partyRoomId),
                userId
        );

        java.time.Instant beforeInstant = before != null ? java.time.Instant.parse(before) : null;

        ChatMessageRepository.PartyChatMessagesPage result =
                chatMessageRepository.findPartyChatMessages(partyRoomId, limit, beforeInstant);

        List<PartyChatMessageResponse> messages = result.messages().stream()
                .map(msg -> new PartyChatMessageResponse(
                        msg.id(),
                        msg.partyRoomId(),
                        msg.senderId(),
                        msg.senderNickname(),
                        msg.content(),
                        msg.type(),
                        msg.timestamp().toString()
                ))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                new PartyChatMessagesPageResponse(messages, result.hasMore(), result.nextCursor())
        ));
    }

    /**
     * 파티 채팅 읽음 처리
     */
    @PostMapping("/{partyRoomId}/read")
    public ResponseEntity<Void> markAsRead(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId) {

        partyRoomService.markChatAsRead(PartyRoomId.of(partyRoomId), userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{partyRoomId}/leave")
    public ResponseEntity<Void> leavePartyRoom(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId) {

        partyRoomService.leavePartyRoom(PartyRoomId.of(partyRoomId), userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{partyRoomId}/complete")
    public ResponseEntity<ApiResponse<PartyRoomResponse>> completePartyRoom(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId) {

        PartyRoom partyRoom = partyRoomService.completePartyRoom(
                PartyRoomId.of(partyRoomId),
                userId
        );

        return ResponseEntity.ok(ApiResponse.success(PartyRoomResponse.from(partyRoom, getCharacterMap(partyRoom))));
    }

    @PostMapping("/{partyRoomId}/ready-check")
    public ResponseEntity<ApiResponse<PartyRoomResponse>> startReadyCheck(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId) {

        PartyRoom partyRoom = partyRoomService.startReadyCheck(
                PartyRoomId.of(partyRoomId),
                userId
        );

        return ResponseEntity.ok(ApiResponse.success(PartyRoomResponse.from(partyRoom, getCharacterMap(partyRoom))));
    }

    @PostMapping("/{partyRoomId}/ready")
    public ResponseEntity<ApiResponse<PartyRoomResponse>> markReady(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId) {

        PartyRoom partyRoom = partyRoomService.markReady(
                PartyRoomId.of(partyRoomId),
                userId
        );

        return ResponseEntity.ok(ApiResponse.success(PartyRoomResponse.from(partyRoom, getCharacterMap(partyRoom))));
    }

    @PostMapping("/{partyRoomId}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> submitReviews(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId,
            @Valid @RequestBody List<ReviewRequest> requests) {

        List<PartyRoomService.ReviewInput> inputs = requests.stream()
                .map(r -> new PartyRoomService.ReviewInput(
                        UserId.of(r.targetUserId()),
                        r.tags(),
                        r.comment()
                ))
                .toList();

        List<Review> reviews = partyRoomService.submitReviews(
                PartyRoomId.of(partyRoomId),
                userId,
                inputs
        );

        List<ReviewResponse> responses = reviews.stream()
                .map(ReviewResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 내 가용시간 저장
     */
    @PutMapping("/{partyRoomId}/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> saveAvailability(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId,
            @Valid @RequestBody AvailabilityRequest request) {

        List<Availability.TimeSlot> slots = request.slots().stream()
                .map(dto -> new Availability.TimeSlot(dto.date(), dto.time()))
                .toList();

        PartyRoomId roomId = PartyRoomId.of(partyRoomId);

        Availability availability = availabilityService.saveAvailability(
                roomId,
                userId,
                slots
        );

        String nickname = availabilityService.getUserNickname(userId);
        String characterName = availabilityService.getCharacterName(roomId, userId);

        return ResponseEntity.ok(ApiResponse.success(
                AvailabilityResponse.from(availability, nickname, characterName)
        ));
    }

    /**
     * 전체 멤버 가용시간 조회 (히트맵 포함)
     */
    @GetMapping("/{partyRoomId}/availability")
    public ResponseEntity<ApiResponse<AllAvailabilityResponse>> getAllAvailability(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId) {

        AllAvailabilityResponse response = availabilityService.getAllAvailability(
                PartyRoomId.of(partyRoomId),
                userId
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== Availability (When2Meet) ====================

    /**
     * 일정 확정 (파티장만)
     */
    @PostMapping("/{partyRoomId}/schedule")
    public ResponseEntity<ApiResponse<PartyRoomResponse>> confirmSchedule(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId,
            @Valid @RequestBody ConfirmScheduleRequest request) {

        PartyRoom partyRoom = availabilityService.confirmSchedule(
                PartyRoomId.of(partyRoomId),
                userId,
                request.scheduledTime()
        );

        return ResponseEntity.ok(ApiResponse.success(PartyRoomResponse.from(partyRoom, getCharacterMap(partyRoom))));
    }

    public record PartyChatMessageResponse(
            String id,
            String partyRoomId,
            String senderId,
            String senderNickname,
            String content,
            String type,
            String timestamp
    ) {
    }

    public record PartyChatMessagesPageResponse(
            List<PartyChatMessageResponse> messages,
            boolean hasMore,
            String nextCursor
    ) {
    }
}
