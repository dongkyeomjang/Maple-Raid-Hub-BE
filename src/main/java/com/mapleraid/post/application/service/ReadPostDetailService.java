package com.mapleraid.post.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.post.application.port.in.input.query.ReadPostDetailInput;
import com.mapleraid.post.application.port.in.output.result.ReadPostDetailResult;
import com.mapleraid.post.application.port.in.output.result.ReadPostDetailResult.CharacterSummary;
import com.mapleraid.post.application.port.in.usecase.ReadPostDetailUseCase;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Application;
import com.mapleraid.post.domain.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReadPostDetailService implements ReadPostDetailUseCase {

    private final PostRepository postRepository;
    private final CharacterRepository characterRepository;

    public ReadPostDetailService(PostRepository postRepository,
                                 CharacterRepository characterRepository) {
        this.postRepository = postRepository;
        this.characterRepository = characterRepository;
    }

    @Override
    public ReadPostDetailResult execute(ReadPostDetailInput input) {
        Post post = postRepository.findByIdWithApplications(input.getPostId())
                .orElseThrow(() -> new CommonException(ErrorCode.POST_NOT_FOUND));

        // 모든 캐릭터 ID 수집 (작성자 + 지원자)
        Set<CharacterId> characterIds = new HashSet<>();
        characterIds.add(post.getCharacterId());
        for (Application app : post.getApplications()) {
            characterIds.add(app.getCharacterId());
        }

        // 배치 조회
        Map<CharacterId, Character> characterMap = characterRepository.findByIds(characterIds).stream()
                .collect(Collectors.toMap(Character::getId, c -> c));

        // 작성자 캐릭터
        CharacterSummary authorCharacter = toCharacterSummary(characterMap.get(post.getCharacterId()));

        // 지원 목록 (캐릭터 정보 포함)
        List<ReadPostDetailResult.ApplicationSummary> appSummaries = post.getApplications().stream()
                .map(app -> new ReadPostDetailResult.ApplicationSummary(
                        app.getId().getValue().toString(),
                        app.getApplicantId().getValue().toString(),
                        app.getCharacterId().getValue().toString(),
                        app.getMessage(),
                        app.getStatus().name(),
                        app.getAppliedAt(),
                        app.getRespondedAt(),
                        toCharacterSummary(characterMap.get(app.getCharacterId()))
                ))
                .toList();

        return new ReadPostDetailResult(
                post.getId().getValue().toString(),
                post.getAuthorId().getValue().toString(),
                post.getCharacterId().getValue().toString(),
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
                post.getExpiresAt(),
                post.getClosedAt(),
                appSummaries,
                authorCharacter);
    }

    private CharacterSummary toCharacterSummary(Character c) {
        if (c == null) return null;
        return new CharacterSummary(
                c.getId().getValue().toString(),
                c.getCharacterName(),
                c.getWorldName(),
                c.getEWorldGroup().name(),
                c.getCharacterClass(),
                c.getCharacterLevel(),
                c.getCharacterImageUrl(),
                c.getCombatPower(),
                c.getEquipmentJson(),
                c.getVerificationStatus().name(),
                c.getLastSyncedAt());
    }
}
