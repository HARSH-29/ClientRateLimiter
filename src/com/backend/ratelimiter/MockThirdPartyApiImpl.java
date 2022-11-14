package com.backend.ratelimiter;

public class MockThirdPartyApiImpl implements ThirdPartyApi {
    @Override
    public boolean getSafetyEventsData(int perEntityId) {
        return true;
    }
}
