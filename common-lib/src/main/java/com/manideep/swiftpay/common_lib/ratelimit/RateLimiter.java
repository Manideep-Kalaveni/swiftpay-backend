package com.manideep.swiftpay.common_lib.ratelimit;

public interface RateLimiter {

    RateLimitResult check(String apiKey, int maxAllowedRequests, long windowSeconds);
}
