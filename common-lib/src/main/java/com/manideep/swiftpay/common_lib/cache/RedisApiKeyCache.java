package com.manideep.swiftpay.common_lib.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisApiKeyCache implements ApiKeyCache{

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    public static final String PREFIX = "apikey_";
    private static final Duration TTL = Duration.ofMinutes(5);

    @Override
    public Optional<ApiKeyCacheEntry> get(String keyId) {
        try{
            String value = stringRedisTemplate.opsForValue().get(PREFIX+keyId);

            if(value == null){
                return Optional.empty();
            }

            ApiKeyCacheEntry entry = objectMapper.readValue(value, ApiKeyCacheEntry.class);

            return Optional.of(entry);
        }
        catch(Exception e){
            log.warn("API Key getting failed : {}", keyId);
            return Optional.empty();
        }
    }

    @Override
    public void put(String keyId, ApiKeyCacheEntry apiKeyCacheEntry) {
        try{
            String value = objectMapper.writeValueAsString(apiKeyCacheEntry);

            stringRedisTemplate.opsForValue()
                    .set(PREFIX + keyId, value, TTL);
        }
        catch(Exception e){
            log.warn("Failed to cache API key. keyId={}", keyId);
        }
    }

    @Override
    public void evict(String keyId) {
        stringRedisTemplate.delete(PREFIX + keyId);
    }
}
