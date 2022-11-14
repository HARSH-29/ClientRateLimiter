package com.backend.ratelimiter;

public interface MyBackend {
    void fetchSystemEventsData(int perEntityId);

    void shutdown();
}
