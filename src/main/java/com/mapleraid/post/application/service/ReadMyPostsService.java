package com.mapleraid.post.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.post.application.port.in.input.query.ReadMyPostsInput;
import com.mapleraid.post.application.port.in.output.result.ReadMyPostsResult;
import com.mapleraid.post.application.port.in.usecase.ReadMyPostsUseCase;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadMyPostsService implements ReadMyPostsUseCase {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;

    @Override
    @Transactional(readOnly = true)
    public ReadMyPostsResult execute(ReadMyPostsInput input) {
        List<Post> posts = postRepository.findByAuthorId(input.getUserId());

        // Collect IDs for batch fetch
        Set<CharacterId> charIds = posts.stream()
                .map(Post::getCharacterId)
                .collect(Collectors.toSet());
        List<UserId> userIds = posts.stream()
                .map(Post::getAuthorId)
                .distinct()
                .toList();

        // Batch fetch users and characters
        Map<UserId, User> userMap = userRepository.findAllByIds(userIds);
        Map<CharacterId, Character> charMap = characterRepository.findByIds(charIds).stream()
                .collect(Collectors.toMap(Character::getId, c -> c));

        // Build summaries with enriched data
        List<ReadMyPostsResult.PostSummary> summaries = posts.stream()
                .map(post -> {
                    User author = userMap.get(post.getAuthorId());
                    Character character = charMap.get(post.getCharacterId());
                    return new ReadMyPostsResult.PostSummary(
                            post.getId().getValue().toString(),
                            post.getAuthorId().getValue().toString(),
                            author != null ? author.getNickname() : null,
                            post.getCharacterId().getValue().toString(),
                            character != null ? character.getCharacterName() : null,
                            character != null ? character.getCharacterImageUrl() : null,
                            post.getWorldGroup().name(),
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

        return new ReadMyPostsResult(summaries);
    }
}
