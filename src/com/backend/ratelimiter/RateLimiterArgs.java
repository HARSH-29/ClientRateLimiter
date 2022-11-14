package com.backend.ratelimiter;

import java.util.concurrent.TimeUnit;

public class RateLimiterArgs {
    int limit; // 1000
    int period; // 60
    TimeUnit timeUnit; // Seconds

    RateLimiterArgs(int limit, int period, TimeUnit timeUnit) {
        this.limit = limit;
        this.period = period;
        this.timeUnit = timeUnit;
    }
}
