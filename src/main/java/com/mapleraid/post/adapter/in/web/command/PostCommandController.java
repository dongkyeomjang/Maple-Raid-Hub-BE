package com.mapleraid.post.adapter.in.web.command;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.core.annotation.bean.CurrentUser;
import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.post.adapter.in.web.dto.request.ApplyToPostRequestDto;
import com.mapleraid.post.adapter.in.web.dto.request.CreatePostRequestDto;
import com.mapleraid.post.adapter.in.web.dto.request.UpdatePostRequestDto;
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
import com.mapleraid.post.application.port.in.output.result.AcceptApplicationResult;
import com.mapleraid.post.application.port.in.usecase.AcceptApplicationUseCase;
import com.mapleraid.post.application.port.in.usecase.ApplyToPostUseCase;
import com.mapleraid.post.application.port.in.usecase.ClosePostUseCase;
import com.mapleraid.post.application.port.in.usecase.CreatePostUseCase;
import com.mapleraid.post.application.port.in.usecase.DeletePostUseCase;
import com.mapleraid.post.application.port.in.usecase.RejectApplicationUseCase;
import com.mapleraid.post.application.port.in.usecase.UpdatePostUseCase;
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

    /**
     * 모집글 생성하기
     */
    @PostMapping
    public ResponseDto<PostResponseDto> createPost(
            @CurrentUser UserId userId,
            @RequestBody CreatePostRequestDto request) {

        return ResponseDto.created(
                PostResponseDto.from(
                        createPostUseCase.execute(
                                CreatePostInput.of(
                                        userId,
                                        CharacterId.of(request.characterId()),
                                        request.bossIds(),
                                        request.requiredMembers(),
                                        request.preferredTime(),
                                        request.description()
                                )
                        )
                )
        );
    }

    /**
     * 모집글 수정하기
     */
    @PatchMapping("/{postId}")
    public ResponseDto<PostResponseDto> updatePost(
            @CurrentUser UserId userId,
            @PathVariable String postId,
            @RequestBody UpdatePostRequestDto request) {

        return ResponseDto.ok(
                PostResponseDto.from(
                        updatePostUseCase.execute(
                                UpdatePostInput.of(
                                        PostId.of(postId),
                                        userId,
                                        request.bossIds(),
                                        request.requiredMembers(),
                                        request.preferredTime(),
                                        request.shouldClearPreferredTime(),
                                        request.description(),
                                        request.shouldClearDescription()
                                )
                        )
                )
        );
    }

    /**
     * 모집글 삭제하기
     */
    @DeleteMapping("/{postId}")
    public ResponseDto<Void> deletePost(
            @CurrentUser UserId userId,
            @PathVariable String postId) {

        deletePostUseCase.execute(
                DeletePostInput.of(
                        PostId.of(postId),
                        userId
                )
        );
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
}
