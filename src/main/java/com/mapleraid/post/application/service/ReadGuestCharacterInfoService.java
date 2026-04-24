package com.mapleraid.post.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.external.application.port.out.NexonApiPort;
import com.mapleraid.post.application.port.in.input.query.ReadGuestCharacterInfoInput;
import com.mapleraid.post.application.port.in.output.result.ReadGuestCharacterInfoResult;
import com.mapleraid.post.application.port.in.usecase.ReadGuestCharacterInfoUseCase;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReadGuestCharacterInfoService implements ReadGuestCharacterInfoUseCase {

    private final PostRepository postRepository;
    private final NexonApiPort nexonApiPort;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public ReadGuestCharacterInfoResult execute(ReadGuestCharacterInfoInput input) {
        Post post = postRepository.findById(input.getPostId())
                .orElseThrow(() -> new CommonException(ErrorCode.POST_NOT_FOUND));

        if (!post.isGuest()) {
            throw new CommonException(ErrorCode.POST_NOT_GUEST);
        }

        String ocid = nexonApiPort.resolveOcid(post.getGuestCharacterName(), post.getGuestWorldName())
                .orElseThrow(() -> new CommonException(ErrorCode.CHARACTER_INFO_UNAVAILABLE));

        NexonApiPort.CharacterBasicInfo basic = nexonApiPort.getCharacterBasic(ocid)
                .orElseThrow(() -> new CommonException(ErrorCode.CHARACTER_INFO_UNAVAILABLE));

        long combatPower = nexonApiPort.getMaxCombatPowerStat(ocid)
                .map(NexonApiPort.CharacterStatInfo::combatPower)
                .orElse(0L);

        String equipmentJson = nexonApiPort.getItemEquipment(ocid)
                .map(this::convertEquipmentToJson)
                .orElse(null);

        return new ReadGuestCharacterInfoResult(
                basic.characterName(),
                basic.worldName(),
                post.getWorldGroup().name(),
                basic.characterClass(),
                basic.characterLevel(),
                basic.characterImage() != null ? basic.characterImage() : post.getGuestCharacterImageUrl(),
                combatPower,
                equipmentJson
        );
    }

    private String convertEquipmentToJson(NexonApiPort.EquipmentInfo equipmentInfo) {
        try {
            return objectMapper.writeValueAsString(equipmentInfo);
        } catch (Exception e) {
            return null;
        }
    }
}
