package com.mapleraid.external.application.port.out;

import java.time.Duration;

/**
 * Rate Limiter 포트
 */
public interface RateLimiterPort {

    /**
     * Rate limit 확인 및 카운트 증가
     *
     * @return 허용되면 true, 제한되면 false
     */
    boolean tryAcquire(String key, int maxRequests, Duration window);

    /**
     * 현재 카운트 조회
     */
    int getCurrentCount(String key);

    /**
     * 다음 허용 시간까지 남은 시간 (초)
     */
    long getSecondsUntilReset(String key);

    /**
     * 키 삭제
     */
    void reset(String key);
}
