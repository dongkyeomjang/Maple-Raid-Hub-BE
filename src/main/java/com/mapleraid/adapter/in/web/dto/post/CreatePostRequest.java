package com.mapleraid.adapter.in.web.dto.post;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreatePostRequest(
        @NotBlank(message = "캐릭터 ID는 필수입니다.")
        String characterId,

        @NotEmpty(message = "최소 1개 이상의 보스를 선택해야 합니다.")
        List<String> bossIds,

        @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
        @Max(value = 6, message = "최대 6명까지 가능합니다.")
        int requiredMembers,

        String preferredTime,

        String description
) {
}
