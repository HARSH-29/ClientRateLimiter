package com.backend.ratelimiter;

import java.util.concurrent.TimeUnit;

;
public class RateLimiterFactory {
    static IRateLimiter getRateLimiter(int limit, int period, TimeUnit timeUnit, RateLimiterType type) {
        if (type == RateLimiterType.SEMAPHORE_BASED) {
            return new RateLimiterImpl(limit, period, timeUnit);
        }
        throw new IllegalArgumentException(String.format("RateLimiter of type: %s not found", type));
    }
}
