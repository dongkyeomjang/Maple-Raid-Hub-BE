package com.mapleraid.notification.application.event;

import java.util.List;

/**
 * 비회원이 모집글을 작성했을 때 관리자에게 알림을 보내기 위해 발행되는 이벤트.
 * 도용/테러글 모니터링 용도.
 */
public record GuestPostCreatedEvent(
        String postId,
        String worldName,
        String characterName,
        String contactLink,
        String description,
        List<String> bossIds
) {
}
