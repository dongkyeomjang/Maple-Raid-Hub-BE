package com.mapleraid.post.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.post.application.port.in.input.query.ReadPostListInput;
import com.mapleraid.post.application.port.in.output.result.ReadPostListResult;
import com.mapleraid.post.application.port.in.usecase.ReadPostListUseCase;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Post;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadPostListService implements ReadPostListUseCase {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;

    @Override
    @Transactional(readOnly = true)
    public ReadPostListResult execute(ReadPostListInput input) {
        List<Post> posts;
        long total;

        boolean hasBossFilter = input.getBossIds() != null && !input.getBossIds().isEmpty();

        if (input.getWorldGroup() != null && hasBossFilter) {
            posts = postRepository.findByWorldGroupAndStatusAndBossIds(
                    input.getWorldGroup(), input.getStatus(), input.getBossIds(), input.getPage(), input.getSize());
            total = postRepository.countByWorldGroupAndStatusAndBossIds(
                    input.getWorldGroup(), input.getStatus(), input.getBossIds());
        } else if (input.getWorldGroup() != null) {
            posts = postRepository.findByWorldGroupAndStatus(
                    input.getWorldGroup(), input.getStatus(), input.getPage(), input.getSize());
            total = postRepository.countByWorldGroupAndStatus(
                    input.getWorldGroup(), input.getStatus());
        } else if (hasBossFilter) {
            posts = postRepository.findByStatusAndBossIds(
                    input.getStatus(), input.getBossIds(), input.getPage(), input.getSize());
            total = postRepository.countByStatusAndBossIds(input.getStatus(), input.getBossIds());
        } else {
            posts = postRepository.findByStatus(
                    input.getStatus(), input.getPage(), input.getSize());
            total = postRepository.countByStatus(input.getStatus());
        }

        // 비회원 글은 authorId/characterId가 null이므로 필터링
        Set<CharacterId> charIds = posts.stream()
                .map(Post::getCharacterId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<UserId> userIds = posts.stream()
                .map(Post::getAuthorId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UserId, User> userMap = userRepository.findAllByIds(userIds);
        Map<CharacterId, Character> charMap = characterRepository.findByIds(charIds).stream()
                .collect(Collectors.toMap(Character::getId, c -> c));

        List<ReadPostListResult.PostSummary> summaries = posts.stream()
                .map(post -> {
                    if (post.isGuest()) {
                        return new ReadPostListResult.PostSummary(
                                post.getId().getValue().toString(),
                                null,
                                null,
                                null,
                                post.getGuestCharacterName(),
                                post.getGuestCharacterImageUrl(),
                                post.getWorldGroup().name(),
                                post.getGuestWorldName(),
                                true,
                                post.getContactLink(),
                                post.getBossIds(),
                                post.getRequiredMembers(),
                                post.getCurrentMembers(),
                                post.getPreferredTime(),
                                post.getDescription(),
                                post.getStatus().name(),
                                null,
                                post.getCreatedAt(),
                                post.getUpdatedAt(),
                                post.getExpiresAt()
                        );
                    }
                    User author = userMap.get(post.getAuthorId());
                    Character character = charMap.get(post.getCharacterId());
                    return new ReadPostListResult.PostSummary(
                            post.getId().getValue().toString(),
                            post.getAuthorId().getValue().toString(),
                            author != null ? author.getNickname() : null,
                            post.getCharacterId().getValue().toString(),
                            character != null ? character.getCharacterName() : null,
                            character != null ? character.getCharacterImageUrl() : null,
                            post.getWorldGroup().name(),
                            character != null ? character.getWorldName() : null,
                            false,
                            null,
                            post.getBossIds(),
                            post.getRequiredMembers(),
                            post.getCurrentMembers(),
                            post.getPreferredTime(),
                            post.getDescription(),
                            post.getStatus().name(),
                            post.getPartyRoomId() != null ? post.getPartyRoomId().getValue().toString() : null,
                            post.getCreatedAt(),
                            post.getUpdatedAt(),
                            post.getExpiresAt()
                    );
                }).toList();

        return new ReadPostListResult(summaries, total, input.getPage(), input.getSize());
    }
}
