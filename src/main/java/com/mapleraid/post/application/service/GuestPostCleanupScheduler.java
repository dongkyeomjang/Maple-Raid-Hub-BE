package com.mapleraid.post.application.service;

import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Post;
import com.mapleraid.post.domain.PostStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * 비회원이 작성한 모집글 중 취소(CANCELED) 상태로 일정 시간이 지난 것을 DB 에서 영구 삭제한다.
 * - 실수 취소에 대비해 24시간의 유예를 둔다.
 * - 회원 모집글은 작성자 기록·파티 히스토리 추적을 위해 삭제하지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GuestPostCleanupScheduler {

    private static final Duration RETENTION = Duration.ofHours(24);

    private final PostRepository postRepository;

    /**
     * 1시간마다 실행. initialDelay=0 이므로 애플리케이션 기동 직후에도 한 번 돌아,
     * 서버 다운타임 동안 쌓인 취소 글도 재기동과 동시에 정리된다.
     */
    @Scheduled(fixedRate = 3_600_000L, initialDelay = 0L)
    @Transactional
    public void cleanupCancelledGuestPosts() {
        Instant cutoff = Instant.now().minus(RETENTION);
        List<Post> posts = postRepository.findGuestByStatusAndClosedAtBefore(PostStatus.CANCELED, cutoff);

        if (posts.isEmpty()) {
            return;
        }

        int deleted = 0;
        for (Post post : posts) {
            try {
                postRepository.delete(post);
                deleted++;
            } catch (Exception e) {
                log.error("[비회원 모집글 정리] 삭제 실패 postId={}: {}",
                        post.getId().getValue(), e.getMessage(), e);
            }
        }

        if (deleted > 0) {
            log.info("[비회원 모집글 정리] 취소된 비회원 모집글 {}건 삭제 완료 (cutoff={})", deleted, cutoff);
        }
    }
}
