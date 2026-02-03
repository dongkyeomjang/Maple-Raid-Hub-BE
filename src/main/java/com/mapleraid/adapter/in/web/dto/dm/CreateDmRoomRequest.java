package com.mapleraid.adapter.in.web.dto.dm;

import jakarta.validation.constraints.NotBlank;

public record CreateDmRoomRequest(
        String postId,  // nullable for general DM

        @NotBlank(message = "대상 사용자 ID는 필수입니다.")
        String targetUserId,

        String senderCharacterId,  // 발신자 캐릭터 ID (optional)

        String targetCharacterId   // 수신자 캐릭터 ID (optional)
) {
}
