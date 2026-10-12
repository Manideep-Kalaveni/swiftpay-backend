package com.manideep.swiftpay.common_lib.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class RedisIdempotencyStore implements IdempotencyStore{

    private final StringRedisTemplate redis;
    private static final String PREFIX = "idempotency:";

    @Override
    public boolean setIfAbsent(String key, Duration ttl) {
        try{
            Boolean set = redis.opsForValue().setIfAbsent(PREFIX+key, IN_PROGRESS, ttl);
            return Boolean.TRUE.equals(set);
        }
        catch(Exception e){
            log.warn("Idempotency store is not available , failing open for key={}", key);
            return true;
        }
    }

    @Override
    public void store(String key, String value, Duration ttl) {
        try{
            redis.opsForValue().set(PREFIX+key, value, ttl);
        }
        catch(Exception e){
            log.warn("Failed to persist, failing open for key={}",key);
        }
    }

    @Override
    public Optional<String> get(String key) {
        try{
            return Optional.ofNullable(redis.opsForValue().get(PREFIX+key));
        }
        catch(Exception e){
            log.warn("Failed to get, failing open for key={}", key);
            return Optional.empty();
        }

    }

    @Override
    public void delete(String key) {
        try{
            redis.delete(PREFIX+key);
        }
        catch(Exception e){
            log.warn("Failed to clear idempotency key={}",key);
        }
    }
}