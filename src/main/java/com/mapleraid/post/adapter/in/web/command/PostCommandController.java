package com.mapleraid.post.adapter.in.web.command;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.character.domain.type.EWorldGroup;
import com.mapleraid.core.annotation.bean.CurrentUser;
import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.post.adapter.in.web.dto.request.ApplyToPostRequestDto;
import com.mapleraid.post.adapter.in.web.dto.request.CreatePostRequestDto;
import com.mapleraid.post.adapter.in.web.dto.request.UpdatePostRequestDto;
import com.mapleraid.post.adapter.in.web.dto.request.VerifyGuestPasswordRequestDto;
import com.mapleraid.post.adapter.in.web.dto.response.AcceptApplicationResponseDto;
import com.mapleraid.post.adapter.in.web.dto.response.ApplicationResponseDto;
import com.mapleraid.post.adapter.in.web.dto.response.PostResponseDto;
import com.mapleraid.post.application.port.in.input.command.AcceptApplicationInput;
import com.mapleraid.post.application.port.in.input.command.ApplyToPostInput;
import com.mapleraid.post.application.port.in.input.command.ClosePostInput;
import com.mapleraid.post.application.port.in.input.command.CreatePostInput;
import com.mapleraid.post.application.port.in.input.command.DeletePostInput;
import com.mapleraid.post.application.port.in.input.command.RejectApplicationInput;
import com.mapleraid.post.application.port.in.input.command.UpdatePostInput;
import com.mapleraid.post.application.port.in.input.command.VerifyGuestPasswordInput;
import com.mapleraid.post.application.port.in.input.command.WithdrawApplicationInput;
import com.mapleraid.post.application.port.in.output.result.AcceptApplicationResult;
import com.mapleraid.post.application.port.in.usecase.AcceptApplicationUseCase;
import com.mapleraid.post.application.port.in.usecase.ApplyToPostUseCase;
import com.mapleraid.post.application.port.in.usecase.ClosePostUseCase;
import com.mapleraid.post.application.port.in.usecase.CreatePostUseCase;
import com.mapleraid.post.application.port.in.usecase.DeletePostUseCase;
import com.mapleraid.post.application.port.in.usecase.RejectApplicationUseCase;
import com.mapleraid.post.application.port.in.usecase.UpdatePostUseCase;
import com.mapleraid.post.application.port.in.usecase.VerifyGuestPasswordUseCase;
import com.mapleraid.post.application.port.in.usecase.WithdrawApplicationUseCase;
import com.mapleraid.post.domain.ApplicationId;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostCommandController {

    private final CreatePostUseCase createPostUseCase;
    private final UpdatePostUseCase updatePostUseCase;
    private final DeletePostUseCase deletePostUseCase;
    private final ClosePostUseCase closePostUseCase;
    private final ApplyToPostUseCase applyToPostUseCase;
    private final AcceptApplicationUseCase acceptApplicationUseCase;
    private final RejectApplicationUseCase rejectApplicationUseCase;
    private final WithdrawApplicationUseCase withdrawApplicationUseCase;
    private final VerifyGuestPasswordUseCase verifyGuestPasswordUseCase;

    /**
     * 모집글 생성하기 (회원 또는 비회원)
     */
    @PostMapping
    public ResponseDto<PostResponseDto> createPost(
            @CurrentUser(required = false) UserId userId,
            @RequestBody CreatePostRequestDto request) {

        CreatePostInput input;
        if (request.isGuest()) {
            if (userId != null) {
                throw new CommonException(ErrorCode.POST_GUEST_FORBIDDEN_FOR_MEMBER);
            }
            EWorldGroup worldGroup;
            try {
                worldGroup = EWorldGroup.valueOf(request.guestWorldGroup());
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new CommonException(ErrorCode.INVALID_WORLD);
            }
            input = CreatePostInput.ofGuest(
                    worldGroup,
                    request.guestWorldName(),
                    request.guestCharacterName(),
                    request.contactLink(),
                    request.guestPassword(),
                    request.bossIds(),
                    request.requiredMembers(),
                    request.preferredTime(),
                    request.description()
            );
        } else {
            if (userId == null) {
                throw new CommonException(ErrorCode.UNAUTHORIZED);
            }
            input = CreatePostInput.of(
                    userId,
                    CharacterId.of(request.characterId()),
                    request.bossIds(),
                    request.requiredMembers(),
                    request.preferredTime(),
                    request.description()
            );
        }

        return ResponseDto.created(
                PostResponseDto.from(createPostUseCase.execute(input))
        );
    }

    /**
     * 모집글 수정하기 (회원 또는 비회원)
     */
    @PatchMapping("/{postId}")
    public ResponseDto<PostResponseDto> updatePost(
            @CurrentUser(required = false) UserId userId,
            @PathVariable String postId,
            @RequestBody UpdatePostRequestDto request) {

        UpdatePostInput input;
        if (userId != null) {
            input = UpdatePostInput.of(
                    PostId.of(postId),
                    userId,
                    request.bossIds(),
                    request.requiredMembers(),
                    request.preferredTime(),
                    request.shouldClearPreferredTime(),
                    request.description(),
                    request.shouldClearDescription()
            );
        } else {
            if (request.guestPassword() == null || request.guestPassword().isBlank()) {
                throw new CommonException(ErrorCode.POST_GUEST_INVALID_PASSWORD);
            }
            input = UpdatePostInput.ofGuest(
                    PostId.of(postId),
                    request.guestPassword(),
                    request.bossIds(),
                    request.requiredMembers(),
                    request.preferredTime(),
                    request.shouldClearPreferredTime(),
                    request.description(),
                    request.shouldClearDescription()
            );
        }

        return ResponseDto.ok(PostResponseDto.from(updatePostUseCase.execute(input)));
    }

    /**
     * 모집글 삭제하기 (회원 또는 비회원)
     */
    @DeleteMapping("/{postId}")
    public ResponseDto<Void> deletePost(
            @CurrentUser(required = false) UserId userId,
            @PathVariable String postId,
            @RequestParam(value = "guestPassword", required = false) String guestPassword) {

        DeletePostInput input;
        if (userId != null) {
            input = DeletePostInput.of(PostId.of(postId), userId);
        } else {
            if (guestPassword == null || guestPassword.isBlank()) {
                throw new CommonException(ErrorCode.POST_GUEST_INVALID_PASSWORD);
            }
            input = DeletePostInput.ofGuest(PostId.of(postId), guestPassword);
        }

        deletePostUseCase.execute(input);
        return ResponseDto.ok(null);
    }

    /**
     * 모집글 마감하기
     */
    @PostMapping("/{postId}/close")
    public ResponseDto<PostResponseDto> closePost(
            @CurrentUser UserId userId,
            @PathVariable String postId) {

        return ResponseDto.ok(
                PostResponseDto.from(
                        closePostUseCase.execute(
                                ClosePostInput.of(
                                        PostId.of(postId),
                                        userId
                                )
                        )
                )
        );
    }

    /**
     * 비회원 모집글 비밀번호 검증
     */
    @PostMapping("/{postId}/guest-verify")
    public ResponseDto<Void> verifyGuestPassword(
            @PathVariable String postId,
            @RequestBody VerifyGuestPasswordRequestDto request) {

        verifyGuestPasswordUseCase.execute(
                VerifyGuestPasswordInput.of(PostId.of(postId), request.password())
        );
        return ResponseDto.ok(null);
    }

    /**
     * 모집글에 지원하기
     */
    @PostMapping("/{postId}/applications")
    public ResponseDto<ApplicationResponseDto> applyToPost(
            @CurrentUser UserId userId,
            @PathVariable String postId,
            @RequestBody ApplyToPostRequestDto request) {

        return ResponseDto.created(
                ApplicationResponseDto.from(
                        applyToPostUseCase.execute(
                                ApplyToPostInput.of(
                                        PostId.of(postId),
                                        userId,
                                        CharacterId.of(request.characterId()),
                                        request.message()
                                )
                        )
                )
        );
    }

    /**
     * 지원 수락하기
     */
    @PostMapping("/{postId}/applications/{applicationId}/accept")
    public ResponseDto<AcceptApplicationResponseDto> acceptApplication(
            @CurrentUser UserId userId,
            @PathVariable String postId,
            @PathVariable String applicationId) {

        AcceptApplicationResult result = acceptApplicationUseCase.execute(
                AcceptApplicationInput.of(
                        PostId.of(postId),
                        ApplicationId.of(applicationId),
                        userId
                )
        );
        return ResponseDto.ok(
                new AcceptApplicationResponseDto(
                        result.getApplicationId(),
                        result.getPartyRoomId()
                )
        );
    }

    /**
     * 지원 거절하기
     */
    @PostMapping("/{postId}/applications/{applicationId}/reject")
    public ResponseDto<ApplicationResponseDto> rejectApplication(
            @CurrentUser UserId userId,
            @PathVariable String postId,
            @PathVariable String applicationId) {

        return ResponseDto.ok(
                ApplicationResponseDto.from(
                        rejectApplicationUseCase.execute(
                                RejectApplicationInput.of(
                                        PostId.of(postId),
                                        ApplicationId.of(applicationId),
                                        userId
                                )
                        )
                )
        );
    }

    /**
     * 지원 취소하기 (지원자가 직접)
     */
    @DeleteMapping("/{postId}/applications/{applicationId}")
    public ResponseDto<ApplicationResponseDto> withdrawApplication(
            @CurrentUser UserId userId,
            @PathVariable String postId,
            @PathVariable String applicationId) {

        return ResponseDto.ok(
                ApplicationResponseDto.from(
                        withdrawApplicationUseCase.execute(
                                WithdrawApplicationInput.of(
                                        PostId.of(postId),
                                        ApplicationId.of(applicationId),
                                        userId
                                )
                        )
                )
        );
    }
}
