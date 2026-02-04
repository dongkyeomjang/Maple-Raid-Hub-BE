package com.mapleraid.external.adapter.out;

import com.mapleraid.external.application.port.out.RateLimiterPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RedisRateLimiterAdapter implements RateLimiterPort {

    private final StringRedisTemplate redisTemplate;

    public RedisRateLimiterAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryAcquire(String key, int maxRequests, Duration window) {
        String redisKey = "rate_limit:" + key;

        Long currentCount = redisTemplate.opsForValue().increment(redisKey);
        if (currentCount == null) {
            return false;
        }

        if (currentCount == 1) {
            redisTemplate.expire(redisKey, window.toSeconds(), TimeUnit.SECONDS);
        }

        return currentCount <= maxRequests;
    }

    @Override
    public int getCurrentCount(String key) {
        String redisKey = "rate_limit:" + key;
        String value = redisTemplate.opsForValue().get(redisKey);
        return value != null ? Integer.parseInt(value) : 0;
    }

    @Override
    public long getSecondsUntilReset(String key) {
        String redisKey = "rate_limit:" + key;
        Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    @Override
    public void reset(String key) {
        String redisKey = "rate_limit:" + key;
        redisTemplate.delete(redisKey);
    }
}
