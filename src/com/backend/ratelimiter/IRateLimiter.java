package com.backend.ratelimiter;

public interface IRateLimiter {

    ThrottleResponse throttle();

    void release();

    void shutdown();
}
