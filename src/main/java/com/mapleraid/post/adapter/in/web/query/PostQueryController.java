package com.mapleraid.post.adapter.in.web.query;

import com.mapleraid.character.domain.type.EWorldGroup;
import com.mapleraid.core.annotation.bean.CurrentUser;
import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.post.adapter.in.web.dto.response.ApplicationsResponseDto;
import com.mapleraid.post.adapter.in.web.dto.response.MyApplicationsListResponseDto;
import com.mapleraid.post.adapter.in.web.dto.response.PostDetailResponseDto;
import com.mapleraid.post.adapter.in.web.dto.response.PostListResponseDto;
import com.mapleraid.post.adapter.in.web.dto.response.PostResponseDto;
import com.mapleraid.post.adapter.in.web.dto.response.ReadMyPostsResponseDto;
import com.mapleraid.post.application.port.in.input.query.ReadMyApplicationsInput;
import com.mapleraid.post.application.port.in.input.query.ReadMyPostsInput;
import com.mapleraid.post.application.port.in.input.query.ReadPostApplicationsInput;
import com.mapleraid.post.application.port.in.input.query.ReadPostDetailInput;
import com.mapleraid.post.application.port.in.input.query.ReadPostListInput;
import com.mapleraid.post.application.port.in.output.result.ReadPostListResult;
import com.mapleraid.post.application.port.in.usecase.ReadMyApplicationsUseCase;
import com.mapleraid.post.application.port.in.usecase.ReadMyPostsUseCase;
import com.mapleraid.post.application.port.in.usecase.ReadPostApplicationsUseCase;
import com.mapleraid.post.application.port.in.usecase.ReadPostDetailUseCase;
import com.mapleraid.post.application.port.in.usecase.ReadPostListUseCase;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.post.domain.PostStatus;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostQueryController {

    private final ReadPostListUseCase readPostListUseCase;
    private final ReadPostDetailUseCase readPostDetailUseCase;
    private final ReadMyApplicationsUseCase readMyApplicationsUseCase;
    private final ReadMyPostsUseCase readMyPostsUseCase;
    private final ReadPostApplicationsUseCase readPostApplicationsUseCase;

    /**
     * 모집글 목록 조회하기
     */
    @GetMapping
    public ResponseDto<PostListResponseDto> listPosts(
            @RequestParam(required = false) EWorldGroup worldGroup,
            @RequestParam(defaultValue = "RECRUITING") PostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> bossIds) {

        ReadPostListResult result = readPostListUseCase.execute(
                ReadPostListInput.of(worldGroup, status, page, size, bossIds));
        List<PostResponseDto> posts = result.getPosts().stream()
                .map(PostResponseDto::from)
                .toList();
        return ResponseDto.ok(
                PostListResponseDto.of(posts, result.getTotal(), result.getPage(), result.getSize()));
    }

    /**
     * 모집글 상세 조회하기
     */
    @GetMapping("/{postId}")
    public ResponseDto<PostDetailResponseDto> getPost(
            @PathVariable String postId) {

        return ResponseDto.ok(
                PostDetailResponseDto.from(
                        readPostDetailUseCase.execute(
                                ReadPostDetailInput.of(
                                        PostId.of(postId)
                                )
                        )
                )
        );
    }

    /**
     * 내 지원 목록 조회하기
     */
    @GetMapping("/my-applications")
    public ResponseDto<MyApplicationsListResponseDto> getMyApplications(
            @CurrentUser UserId userId) {

        return ResponseDto.ok(
                MyApplicationsListResponseDto.from(
                        readMyApplicationsUseCase.execute(
                                ReadMyApplicationsInput.of(userId)
                        )
                )
        );
    }

    /**
     * 내 모집글 목록 조회하기
     */
    @GetMapping("/my-posts")
    public ResponseDto<ReadMyPostsResponseDto> getMyPosts(
            @CurrentUser UserId userId) {

        return ResponseDto.ok(
                ReadMyPostsResponseDto.from(
                        readMyPostsUseCase.execute(
                                ReadMyPostsInput.of(userId)
                        )
                )
        );
    }

    /**
     * 모집글 지원 목록 조회하기
     */
    @GetMapping("/{postId}/applications")
    public ResponseDto<ApplicationsResponseDto> getApplications(
            @CurrentUser UserId userId,
            @PathVariable String postId) {

        return ResponseDto.ok(
                ApplicationsResponseDto.from(
                        readPostApplicationsUseCase.execute(
                                ReadPostApplicationsInput.of(PostId.of(postId), userId)
                        )
                )
        );
    }
}
