package com.manideep.swiftpay.common_lib.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class FixedWindowCounterRateLimiter implements RateLimiter{

    private final StringRedisTemplate redis;

    @Override
    public RateLimitResult check(String apiKey, int maxAllowedRequests, long windowSeconds) {
        String redisKey = "ratelimit:fixed:"+apiKey;
        Long count = redis.opsForValue().increment(redisKey);

        if(count == null) return RateLimitResult.allowed(maxAllowedRequests);

        if(count == 1){
            redis.expire(redisKey, Duration.ofSeconds(windowSeconds));
        }

        if(count > maxAllowedRequests){
            Long ttl = redis.getExpire(apiKey, TimeUnit.SECONDS);

            int retryAfter = (ttl != null && ttl > 0) ? ttl.intValue() : (int)windowSeconds;
            return RateLimitResult.denied(retryAfter);
        }
        return RateLimitResult.allowed((int) (maxAllowedRequests - count));
    }
}
