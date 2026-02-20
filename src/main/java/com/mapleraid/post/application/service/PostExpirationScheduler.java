package com.mapleraid.post.application.service;

import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostExpirationScheduler {

    private final PostRepository postRepository;

    @Scheduled(fixedRate = 600_000) // 10분마다
    @Transactional
    public void expireOldPosts() {
        List<Post> expiredPosts = postRepository.findExpiredRecruiting(Instant.now());

        if (expiredPosts.isEmpty()) {
            return;
        }

        int closedCount = 0;
        int expiredCount = 0;

        for (Post post : expiredPosts) {
            try {
                post.checkAndExpire();
                postRepository.save(post);

                if (post.getStatus() == com.mapleraid.post.domain.PostStatus.CLOSED) {
                    closedCount++;
                } else {
                    expiredCount++;
                }
            } catch (Exception e) {
                log.error("[모집글 만료] 처리 실패 postId={}: {}", post.getId().getValue(), e.getMessage(), e);
            }
        }

        log.info("[모집글 만료] 처리 완료: 자동마감 {}건, 만료 {}건", closedCount, expiredCount);
    }
}
