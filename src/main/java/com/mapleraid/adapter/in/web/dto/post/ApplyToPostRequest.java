package com.mapleraid.adapter.in.web.dto.post;

import jakarta.validation.constraints.NotBlank;

public record ApplyToPostRequest(
        @NotBlank(message = "캐릭터 ID는 필수입니다.")
        String characterId,

        String message
) {
}
