package com.mapleraid.application.service;

import com.mapleraid.application.port.out.CharacterRepository;
import com.mapleraid.application.port.out.PartyRoomRepository;
import com.mapleraid.application.port.out.PostRepository;
import com.mapleraid.application.port.out.UserRepository;
import com.mapleraid.domain.character.Character;
import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.character.VerificationStatus;
import com.mapleraid.domain.character.WorldGroup;
import com.mapleraid.domain.common.DomainException;
import com.mapleraid.domain.party.PartyRoom;
import com.mapleraid.domain.post.Application;
import com.mapleraid.domain.post.ApplicationId;
import com.mapleraid.domain.post.Post;
import com.mapleraid.domain.post.PostId;
import com.mapleraid.domain.post.PostStatus;
import com.mapleraid.domain.user.User;
import com.mapleraid.domain.user.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final CharacterRepository characterRepository;
    private final PartyRoomRepository partyRoomRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository,
                       CharacterRepository characterRepository,
                       PartyRoomRepository partyRoomRepository,
                       UserRepository userRepository) {
        this.postRepository = postRepository;
        this.characterRepository = characterRepository;
        this.partyRoomRepository = partyRoomRepository;
        this.userRepository = userRepository;
    }

    /**
     * 모집글 생성
     */
    public Post createPost(UserId authorId, CharacterId characterId,
                           List<String> bossIds, int requiredMembers,
                           String preferredTime, String description) {
        // 캐릭터 확인
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new DomainException("CHARACTER_NOT_FOUND",
                        "캐릭터를 찾을 수 없습니다."));

        // 본인 캐릭터인지 확인
        if (!character.getOwnerId().equals(authorId)) {
            throw new DomainException("CHARACTER_NOT_OWNER",
                    "본인의 캐릭터만 사용할 수 있습니다.");
        }

        // 인증된 캐릭터인지 확인
        if (character.getVerificationStatus() != VerificationStatus.VERIFIED_OWNER) {
            throw new DomainException("POST_REQUIRES_VERIFIED_CHARACTER",
                    "모집글 작성을 위해 인증된 캐릭터가 필요합니다.",
                    Map.of("requiredStatus", VerificationStatus.VERIFIED_OWNER));
        }

        // 모집글 생성
        Post post = Post.create(
                authorId,
                characterId,
                character.getWorldGroup(),
                bossIds,
                requiredMembers,
                preferredTime,
                description
        );

        return postRepository.save(post);
    }

    /**
     * 모집글 목록 조회
     */
    @Transactional(readOnly = true)
    public PostListResult listPosts(WorldGroup worldGroup, PostStatus status, int page, int size) {
        List<Post> posts;
        long total;

        if (worldGroup != null) {
            posts = postRepository.findByWorldGroupAndStatus(worldGroup, status, page, size);
            total = postRepository.countByWorldGroupAndStatus(worldGroup, status);
        } else {
            posts = postRepository.findByStatus(status, page, size);
            total = postRepository.countByStatus(status);
        }

        return new PostListResult(posts, total, page, size);
    }

    /**
     * 모집글 상세 조회
     */
    @Transactional(readOnly = true)
    public Post getPost(PostId postId) {
        return postRepository.findByIdWithApplications(postId)
                .orElseThrow(() -> new DomainException("POST_NOT_FOUND",
                        "모집글을 찾을 수 없습니다."));
    }

    /**
     * 모집글 수정
     */
    public Post updatePost(PostId postId, UserId requesterId,
                           List<String> bossIds, Integer requiredMembers,
                           String preferredTime, boolean clearPreferredTime,
                           String description, boolean clearDescription) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new DomainException("POST_NOT_FOUND",
                        "모집글을 찾을 수 없습니다."));

        if (!post.isAuthor(requesterId)) {
            throw new DomainException("POST_NOT_AUTHOR",
                    "본인의 모집글만 수정할 수 있습니다.");
        }

        post.update(bossIds, requiredMembers, preferredTime, clearPreferredTime,
                description, clearDescription);

        return postRepository.save(post);
    }

    /**
     * 모집글 삭제
     */
    public void deletePost(PostId postId, UserId requesterId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new DomainException("POST_NOT_FOUND",
                        "모집글을 찾을 수 없습니다."));

        if (!post.isAuthor(requesterId)) {
            throw new DomainException("POST_NOT_AUTHOR",
                    "본인의 모집글만 삭제할 수 있습니다.");
        }

        post.cancel();
        postRepository.save(post);
    }

    /**
     * 모집 마감
     */
    public Post closePost(PostId postId, UserId requesterId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new DomainException("POST_NOT_FOUND",
                        "모집글을 찾을 수 없습니다."));

        if (!post.isAuthor(requesterId)) {
            throw new DomainException("POST_NOT_AUTHOR",
                    "본인의 모집글만 마감할 수 있습니다.");
        }

        post.close();

        return postRepository.save(post);
    }

    /**
     * 파티 지원
     */
    public Application applyToPost(PostId postId, UserId applicantId,
                                   CharacterId characterId, String message) {
        Post post = postRepository.findByIdWithApplications(postId)
                .orElseThrow(() -> new DomainException("POST_NOT_FOUND",
                        "모집글을 찾을 수 없습니다."));

        // 캐릭터 확인
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new DomainException("CHARACTER_NOT_FOUND",
                        "캐릭터를 찾을 수 없습니다."));

        // 본인 캐릭터인지 확인
        if (!character.getOwnerId().equals(applicantId)) {
            throw new DomainException("CHARACTER_NOT_OWNER",
                    "본인의 캐릭터만 사용할 수 있습니다.");
        }

        // 인증된 캐릭터인지 확인
        if (character.getVerificationStatus() != VerificationStatus.VERIFIED_OWNER) {
            throw new DomainException("APPLICATION_REQUIRES_VERIFIED_CHARACTER",
                    "지원하려면 인증된 캐릭터가 필요합니다.");
        }

        // 지원 (도메인에서 월드그룹 체크 등 수행)
        Application application = post.apply(
                applicantId,
                characterId,
                character.getWorldGroup(),
                message
        );

        postRepository.save(post);

        return application;
    }

    /**
     * 지원 수락
     */
    public AcceptResult acceptApplication(PostId postId, ApplicationId applicationId, UserId requesterId) {
        Post post = postRepository.findByIdWithApplications(postId)
                .orElseThrow(() -> new DomainException("POST_NOT_FOUND",
                        "모집글을 찾을 수 없습니다."));

        if (!post.isAuthor(requesterId)) {
            throw new DomainException("POST_NOT_AUTHOR",
                    "모집글 작성자만 지원을 처리할 수 있습니다.");
        }

        post.acceptApplication(applicationId);

        // 파티룸이 없으면 생성
        PartyRoom partyRoom = partyRoomRepository.findByPostId(postId)
                .orElseGet(() -> {
                    Character authorChar = characterRepository.findById(post.getCharacterId())
                            .orElseThrow(() -> new DomainException("CHARACTER_NOT_FOUND", ""));

                    PartyRoom newRoom = PartyRoom.create(
                            postId,
                            post.getBossIds(),
                            post.getAuthorId(),
                            post.getCharacterId()
                    );

                    post.linkPartyRoom(newRoom.getId());
                    return partyRoomRepository.save(newRoom);
                });

        // 지원자를 파티룸에 추가
        Application acceptedApp = post.getApplications().stream()
                .filter(app -> app.getId().equals(applicationId))
                .findFirst()
                .orElseThrow();

        partyRoom.addMember(
                acceptedApp.getApplicantId(),
                acceptedApp.getCharacterId()
        );

        partyRoomRepository.save(partyRoom);
        postRepository.save(post);

        return new AcceptResult(acceptedApp, partyRoom);
    }

    /**
     * 지원 거절
     */
    public Application rejectApplication(PostId postId, ApplicationId applicationId, UserId requesterId) {
        Post post = postRepository.findByIdWithApplications(postId)
                .orElseThrow(() -> new DomainException("POST_NOT_FOUND",
                        "모집글을 찾을 수 없습니다."));

        if (!post.isAuthor(requesterId)) {
            throw new DomainException("POST_NOT_AUTHOR",
                    "모집글 작성자만 지원을 처리할 수 있습니다.");
        }

        post.rejectApplication(applicationId);
        postRepository.save(post);

        return post.getApplications().stream()
                .filter(app -> app.getId().equals(applicationId))
                .findFirst()
                .orElseThrow();
    }

    /**
     * 지원 취소
     */
    public void cancelApplication(ApplicationId applicationId, UserId requesterId) {
        // TODO: ApplicationRepository 필요하거나 Post에서 찾기
    }

    /**
     * 내 지원 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Application> getMyApplications(UserId userId) {
        return postRepository.findApplicationsByApplicantId(userId);
    }

    /**
     * 내 모집글 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Post> getMyPosts(UserId userId) {
        return postRepository.findByAuthorId(userId);
    }

    /**
     * 지원자들의 캐릭터 정보 조회
     */
    @Transactional(readOnly = true)
    public Map<CharacterId, Character> getCharactersByIds(List<CharacterId> characterIds) {
        return characterIds.stream()
                .distinct()
                .map(id -> characterRepository.findById(id).orElse(null))
                .filter(c -> c != null)
                .collect(java.util.stream.Collectors.toMap(
                        Character::getId,
                        c -> c
                ));
    }

    /**
     * 유저들의 정보 조회
     */
    @Transactional(readOnly = true)
    public Map<UserId, User> getUsersByIds(List<UserId> userIds) {
        return userRepository.findAllByIds(userIds);
    }

    public record PostListResult(List<Post> posts, long total, int page, int size) {
        public int totalPages() {
            return (int) Math.ceil((double) total / size);
        }
    }

    public record AcceptResult(Application application, PartyRoom partyRoom) {
    }
}
