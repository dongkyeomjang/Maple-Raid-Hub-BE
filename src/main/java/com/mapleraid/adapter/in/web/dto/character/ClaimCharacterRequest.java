package com.mapleraid.adapter.in.web.dto.character;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClaimCharacterRequest(
        @NotBlank(message = "캐릭터 이름은 필수입니다.")
        @Size(min = 2, max = 12, message = "캐릭터 이름은 2~12자 사이여야 합니다.")
        String characterName,

        @NotBlank(message = "월드 이름은 필수입니다.")
        String worldName
) {
}
