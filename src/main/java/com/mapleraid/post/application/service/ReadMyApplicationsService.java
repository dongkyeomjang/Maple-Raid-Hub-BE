package com.mapleraid.post.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.post.application.port.in.input.query.ReadMyApplicationsInput;
import com.mapleraid.post.application.port.in.output.result.ReadMyApplicationsResult;
import com.mapleraid.post.application.port.in.usecase.ReadMyApplicationsUseCase;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Application;
import com.mapleraid.post.domain.Post;
import com.mapleraid.post.domain.PostId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadMyApplicationsService implements ReadMyApplicationsUseCase {

    private final PostRepository postRepository;
    private final CharacterRepository characterRepository;

    @Override
    @Transactional(readOnly = true)
    public ReadMyApplicationsResult execute(ReadMyApplicationsInput input) {
        List<Application> applications = postRepository.findApplicationsByApplicantId(input.getUserId());

        // Collect unique post IDs and fetch posts
        Set<String> postIdStrs = applications.stream()
                .map(app -> app.getPostId().getValue().toString())
                .collect(Collectors.toSet());

        Map<String, Post> postMap = new HashMap<>();
        for (String postIdStr : postIdStrs) {
            postRepository.findById(PostId.of(postIdStr))
                    .ifPresent(post -> postMap.put(postIdStr, post));
        }

        // Collect character IDs from posts for author character info
        Set<CharacterId> charIds = postMap.values().stream()
                .map(Post::getCharacterId)
                .collect(Collectors.toSet());

        Map<CharacterId, Character> charMap = characterRepository.findByIds(charIds).stream()
                .collect(Collectors.toMap(Character::getId, c -> c));

        // Build enriched summaries
        List<ReadMyApplicationsResult.ApplicationSummary> summaries = applications.stream()
                .map(app -> {
                    String postIdStr = app.getPostId().getValue().toString();
                    Post post = postMap.get(postIdStr);
                    Character authorChar = post != null ? charMap.get(post.getCharacterId()) : null;

                    return new ReadMyApplicationsResult.ApplicationSummary(
                            app.getId().getValue().toString(),
                            postIdStr,
                            app.getApplicantId().getValue().toString(),
                            app.getCharacterId().getValue().toString(),
                            app.getMessage(),
                            app.getStatus().name(),
                            app.getAppliedAt(),
                            app.getRespondedAt(),
                            post != null ? post.getBossIds() : List.of(),
                            post != null ? post.getStatus().name() : null,
                            post != null ? post.getRequiredMembers() : 0,
                            post != null ? post.getCurrentMembers() : 0,
                            authorChar != null ? authorChar.getCharacterName() : null,
                            authorChar != null ? authorChar.getCharacterImageUrl() : null
                    );
                }).toList();

        return new ReadMyApplicationsResult(summaries);
    }
}
