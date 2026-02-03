package com.mapleraid.adapter.in.web.dto.dm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendDmRequest(
        @NotBlank(message = "메시지 내용은 필수입니다.")
        @Size(max = 2000, message = "메시지는 2000자를 초과할 수 없습니다.")
        String content,

        String senderCharacterId  // nullable
) {
}
