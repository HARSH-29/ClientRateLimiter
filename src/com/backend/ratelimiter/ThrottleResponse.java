package com.backend.ratelimiter;

public class ThrottleResponse {
    private final boolean allowed;
    private int waitTimeMillis;

    ThrottleResponse(boolean allowed) {
        this.allowed = allowed;
    }

    void setWaitTimeMillis(int timeMillis) {
        this.waitTimeMillis = timeMillis;
    }

    int getWaitTimeMillis() {
        return this.waitTimeMillis;
    }

    boolean isAllowed() {
        return this.allowed;
    }
}
