package com.manideep.swiftpay.api_gateway_service.security;

import com.manideep.swiftpay.api_gateway_service.client.ApiKeyLookupClient;
import com.manideep.swiftpay.common_lib.cache.ApiKeyCache;
import com.manideep.swiftpay.common_lib.cache.ApiKeyCacheEntry;
import com.manideep.swiftpay.common_lib.exception.RateLimitException;
import com.manideep.swiftpay.common_lib.ratelimit.RateLimitResult;
import com.manideep.swiftpay.common_lib.ratelimit.RateLimiter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthHandler {
    private static String BASIC_PREFIX = "Basic ";
    private final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final ApiKeyCache apiKeyCache;
    private final RateLimiter rateLimiter;
    private final ApiKeyLookupClient apiKeyLookupClient;


    @Value("${app.rate-limit.use-case.api-key.requests-per-minute:60}")
    private Integer noOfRequestsAllowed;

    public Map<String,String> authenticate(String authHeader, HttpServletResponse response) throws BadRequestException {
        String[] credentials = decode(authHeader);
        if(credentials == null){
            throw new BadRequestException("Malformed API Key Header");
        }

        String keyId = credentials[0];
        String rawSecret = credentials[1];
        // Will cache this .
        //ApiKey apiKey = apiKeyRepository.findByKeyId(keyId).orElseThrow(() -> new BadRequestException("Invalid or missing Api Key"));
        ApiKeyCacheEntry apiKeyCacheEntry = apiKeyCache.get(keyId).orElseGet(() -> loadCache(keyId));

        if(apiKeyCacheEntry == null || !apiKeyCacheEntry.enabled() || !secretMatches(rawSecret, apiKeyCacheEntry)){
            throw new BadRequestException("Invalid or missing API Key");
        }


        RateLimitResult rateLimitResult = rateLimiter.check("apikey:"+keyId, noOfRequestsAllowed, 60);

        if(!rateLimitResult.isAllowed()){
            log.warn("Too many Requests keyId={}",keyId);
            throw new RateLimitException("Too Many Requests ", rateLimitResult.retryAfterSeconds());
        }

        response.setHeader("X-RateLimit-Result", String.valueOf(noOfRequestsAllowed));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(rateLimitResult.remaining()));

        return Map.of(
                "X-Merchant-Id", apiKeyCacheEntry.merchantId().toString(),
                "X-Environment", apiKeyCacheEntry.environment().name(),
                "X-Key-Id", apiKeyCacheEntry.keyId()
        );
    }

    private ApiKeyCacheEntry loadCache(String keyId) {

        try{
            ApiKeyCacheEntry entry = apiKeyLookupClient.findByKeyId(keyId);
            apiKeyCache.put(keyId, entry);
            return entry;
        }
        catch (Exception e){
            log.warn("API Key look up failed:{}",keyId);
            return null;
        }
    }

    private boolean secretMatches(String rawSecret, ApiKeyCacheEntry apiKey){
        if(BCRYPT.matches(rawSecret,apiKey.keySecretHash())){
            return true;
        }

        boolean isInGracePeriod = apiKey.gracePeriodExpiresAt() != null && LocalDateTime.now().isBefore(apiKey.gracePeriodExpiresAt());
        return isInGracePeriod && apiKey.previousKeySecretHash() != null && BCRYPT.matches(rawSecret,apiKey.previousKeySecretHash());
    }

    private String[] decode(String header){
        String encoded = header.substring(BASIC_PREFIX.length());
        String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);

        int colon = decoded.indexOf(":");
        if(colon < 1) return null;
        return new String[]{decoded.substring(0,colon), decoded.substring(colon)};
    }
}