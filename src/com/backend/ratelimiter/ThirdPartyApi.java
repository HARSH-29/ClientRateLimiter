package com.backend.ratelimiter;

public interface ThirdPartyApi {
    boolean getSafetyEventsData(int perEntityId);
}
