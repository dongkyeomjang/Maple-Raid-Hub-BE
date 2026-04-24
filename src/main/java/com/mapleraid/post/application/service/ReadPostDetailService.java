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
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadPostDetailService implements ReadPostDetailUseCase {

    private final PostRepository postRepository;
    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ReadPostDetailResult execute(ReadPostDetailInput input) {
        Post post = postRepository.findByIdWithApplications(input.getPostId())
                .orElseThrow(() -> new CommonException(ErrorCode.POST_NOT_FOUND));

        if (post.isGuest()) {
            return toGuestResult(post);
        }

        // 모든 캐릭터 ID 수집 (작성자 + 지원자)
        Set<CharacterId> characterIds = new HashSet<>();
        characterIds.add(post.getCharacterId());
        for (Application app : post.getApplications()) {
            characterIds.add(app.getCharacterId());
        }

        Map<CharacterId, Character> characterMap = characterRepository.findByIds(characterIds).stream()
                .collect(Collectors.toMap(Character::getId, c -> c));

        List<UserId> ownerIdList = characterMap.values().stream()
                .map(Character::getOwnerId)
                .distinct()
                .toList();
        Map<UserId, User> userMap = userRepository.findAllByIds(ownerIdList);

        Character authorChar = characterMap.get(post.getCharacterId());
        double authorTemperature = authorChar != null && userMap.containsKey(authorChar.getOwnerId())
                ? userMap.get(authorChar.getOwnerId()).getTemperature() : 0.0;
        CharacterSummary authorCharacter = toCharacterSummary(authorChar, authorTemperature);

        List<ReadPostDetailResult.ApplicationSummary> appSummaries = post.getApplications().stream()
                .map(app -> {
                    Character appChar = characterMap.get(app.getCharacterId());
                    double temperature = appChar != null && userMap.containsKey(appChar.getOwnerId())
                            ? userMap.get(appChar.getOwnerId()).getTemperature() : 0.0;
                    return new ReadPostDetailResult.ApplicationSummary(
                            app.getId().getValue().toString(),
                            app.getApplicantId().getValue().toString(),
                            app.getCharacterId().getValue().toString(),
                            app.getMessage(),
                            app.getStatus().name(),
                            app.getAppliedAt(),
                            app.getRespondedAt(),
                            toCharacterSummary(appChar, temperature)
                    );
                })
                .toList();

        return new ReadPostDetailResult(
                post.getId().getValue().toString(),
                post.getAuthorId().getValue().toString(),
                post.getCharacterId().getValue().toString(),
                post.getWorldGroup().name(),
                false, null, null, null, null,
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

    private ReadPostDetailResult toGuestResult(Post post) {
        // 비회원 글: 저장된 guest 값으로 기본 캐릭터 카드 구성. 전투력/장비는 별도 엔드포인트에서 매번 조회
        CharacterSummary guestCharacter = new CharacterSummary(
                null,
                post.getGuestCharacterName(),
                post.getGuestWorldName(),
                post.getWorldGroup().name(),
                null,
                0,
                post.getGuestCharacterImageUrl(),
                0L,
                null,
                null,
                null,
                0.0
        );

        return new ReadPostDetailResult(
                post.getId().getValue().toString(),
                null,
                null,
                post.getWorldGroup().name(),
                true,
                post.getGuestWorldName(),
                post.getGuestCharacterName(),
                post.getGuestCharacterImageUrl(),
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
                post.getExpiresAt(),
                post.getClosedAt(),
                List.of(),
                guestCharacter);
    }

    private CharacterSummary toCharacterSummary(Character c, double temperature) {
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
                c.getLastSyncedAt(),
                temperature);
    }
}
