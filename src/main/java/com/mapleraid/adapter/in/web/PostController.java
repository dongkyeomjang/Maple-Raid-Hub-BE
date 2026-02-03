package com.mapleraid.adapter.in.web;

import com.mapleraid.adapter.in.web.dto.ApiResponse;
import com.mapleraid.adapter.in.web.dto.PageResponse;
import com.mapleraid.adapter.in.web.dto.character.PublicCharacterResponse;
import com.mapleraid.adapter.in.web.dto.post.ApplicationResponse;
import com.mapleraid.adapter.in.web.dto.post.ApplicationWithCharacterResponse;
import com.mapleraid.adapter.in.web.dto.post.ApplyToPostRequest;
import com.mapleraid.adapter.in.web.dto.post.CreatePostRequest;
import com.mapleraid.adapter.in.web.dto.post.PostResponse;
import com.mapleraid.adapter.in.web.dto.post.UpdatePostRequest;
import com.mapleraid.adapter.in.web.security.CurrentUser;
import com.mapleraid.application.service.PostService;
import com.mapleraid.application.service.PostService.AcceptResult;
import com.mapleraid.application.service.PostService.PostListResult;
import com.mapleraid.domain.character.Character;
import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.character.WorldGroup;
import com.mapleraid.domain.post.Application;
import com.mapleraid.domain.post.ApplicationId;
import com.mapleraid.domain.post.Post;
import com.mapleraid.domain.post.PostId;
import com.mapleraid.domain.post.PostStatus;
import com.mapleraid.domain.user.User;
import com.mapleraid.domain.user.UserId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @CurrentUser UserId userId,
            @Valid @RequestBody CreatePostRequest request) {

        Post post = postService.createPost(
                userId,
                CharacterId.of(request.characterId()),
                request.bossIds(),
                request.requiredMembers(),
                request.preferredTime(),
                request.description()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(PostResponse.from(post)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> listPosts(
            @RequestParam(required = false) WorldGroup worldGroup,
            @RequestParam(defaultValue = "RECRUITING") PostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PostListResult result = postService.listPosts(worldGroup, status, page, size);

        // 작성자 닉네임 조회
        List<UserId> authorIds = result.posts().stream()
                .map(Post::getAuthorId)
                .distinct()
                .toList();
        Map<UserId, User> userMap = postService.getUsersByIds(authorIds);

        // 캐릭터 정보 조회
        List<CharacterId> characterIds = result.posts().stream()
                .map(Post::getCharacterId)
                .distinct()
                .toList();
        Map<CharacterId, Character> characterMap = postService.getCharactersByIds(characterIds);

        List<PostResponse> responses = result.posts().stream()
                .map(post -> {
                    User author = userMap.get(post.getAuthorId());
                    String nickname = author != null ? author.getNickname() : null;
                    Character character = characterMap.get(post.getCharacterId());
                    return PostResponse.from(post, nickname, character);
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(responses, page, size, result.total())
        ));
    }

    @GetMapping("/my-applications")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getMyApplications(
            @CurrentUser UserId userId) {

        List<Application> applications = postService.getMyApplications(userId);
        List<ApplicationResponse> responses = applications.stream()
                .map(ApplicationResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/my-posts")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getMyPosts(
            @CurrentUser UserId userId) {

        List<Post> posts = postService.getMyPosts(userId);

        // 본인의 닉네임 조회
        Map<UserId, User> userMap = postService.getUsersByIds(List.of(userId));
        User currentUser = userMap.get(userId);
        String nickname = currentUser != null ? currentUser.getNickname() : null;

        // 캐릭터 정보 조회
        List<CharacterId> characterIds = posts.stream()
                .map(Post::getCharacterId)
                .distinct()
                .toList();
        Map<CharacterId, Character> characterMap = postService.getCharactersByIds(characterIds);

        List<PostResponse> responses = posts.stream()
                .map(post -> {
                    Character character = characterMap.get(post.getCharacterId());
                    return PostResponse.from(post, nickname, character);
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(@PathVariable String postId) {
        Post post = postService.getPost(PostId.of(postId));

        // 지원자들의 캐릭터 정보 조회
        List<CharacterId> characterIds = post.getApplications().stream()
                .map(Application::getCharacterId)
                .toList();
        Map<CharacterId, Character> characterMap = postService.getCharactersByIds(characterIds);

        // 작성자 캐릭터 정보도 조회
        Character authorCharacter = characterMap.computeIfAbsent(
                post.getCharacterId(),
                id -> postService.getCharactersByIds(List.of(id)).get(id)
        );

        List<ApplicationWithCharacterResponse> applications = post.getApplications().stream()
                .map(app -> ApplicationWithCharacterResponse.from(app, characterMap.get(app.getCharacterId())))
                .toList();

        PublicCharacterResponse authorCharInfo = authorCharacter != null
                ? PublicCharacterResponse.from(authorCharacter)
                : null;

        return ResponseEntity.ok(ApiResponse.success(
                new PostDetailResponse(PostResponse.from(post), applications, authorCharInfo)
        ));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @CurrentUser UserId userId,
            @PathVariable String postId,
            @Valid @RequestBody UpdatePostRequest request) {

        Post post = postService.updatePost(
                PostId.of(postId),
                userId,
                request.bossIds(),
                request.requiredMembers(),
                request.preferredTime(),
                request.shouldClearPreferredTime(),
                request.description(),
                request.shouldClearDescription()
        );

        return ResponseEntity.ok(ApiResponse.success(PostResponse.from(post)));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @CurrentUser UserId userId,
            @PathVariable String postId) {

        postService.deletePost(PostId.of(postId), userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/close")
    public ResponseEntity<ApiResponse<PostResponse>> closePost(
            @CurrentUser UserId userId,
            @PathVariable String postId) {

        Post post = postService.closePost(PostId.of(postId), userId);
        return ResponseEntity.ok(ApiResponse.success(PostResponse.from(post)));
    }

    @GetMapping("/{postId}/applications")
    public ResponseEntity<ApiResponse<List<ApplicationWithCharacterResponse>>> getApplications(
            @PathVariable String postId) {

        Post post = postService.getPost(PostId.of(postId));

        // 지원자들의 캐릭터 정보 조회
        List<CharacterId> characterIds = post.getApplications().stream()
                .map(Application::getCharacterId)
                .toList();
        Map<CharacterId, Character> characterMap = postService.getCharactersByIds(characterIds);

        List<ApplicationWithCharacterResponse> applications = post.getApplications().stream()
                .map(app -> ApplicationWithCharacterResponse.from(app, characterMap.get(app.getCharacterId())))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @PostMapping("/{postId}/applications")
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyToPost(
            @CurrentUser UserId userId,
            @PathVariable String postId,
            @Valid @RequestBody ApplyToPostRequest request) {

        Application application = postService.applyToPost(
                PostId.of(postId),
                userId,
                CharacterId.of(request.characterId()),
                request.message()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(ApplicationResponse.from(application)));
    }

    @PostMapping("/{postId}/applications/{applicationId}/accept")
    public ResponseEntity<ApiResponse<Map<String, Object>>> acceptApplication(
            @CurrentUser UserId userId,
            @PathVariable String postId,
            @PathVariable String applicationId) {

        AcceptResult result = postService.acceptApplication(
                PostId.of(postId),
                ApplicationId.of(applicationId),
                userId
        );

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "application", ApplicationResponse.from(result.application()),
                "partyRoomId", result.partyRoom().getId().getValue()
        )));
    }

    @PostMapping("/{postId}/applications/{applicationId}/reject")
    public ResponseEntity<ApiResponse<ApplicationResponse>> rejectApplication(
            @CurrentUser UserId userId,
            @PathVariable String postId,
            @PathVariable String applicationId) {

        Application application = postService.rejectApplication(
                PostId.of(postId),
                ApplicationId.of(applicationId),
                userId
        );

        return ResponseEntity.ok(ApiResponse.success(ApplicationResponse.from(application)));
    }

    public record PostDetailResponse(
            PostResponse post,
            List<ApplicationWithCharacterResponse> applications,
            PublicCharacterResponse authorCharacter
    ) {
    }
}
