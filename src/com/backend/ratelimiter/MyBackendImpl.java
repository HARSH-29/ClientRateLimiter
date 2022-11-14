package com.backend.ratelimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyBackendImpl implements MyBackend {
    private final IRateLimiter userRateLimiter;
    private final RateLimiterArgs perEntityLimitArgs;
    private final ThirdPartyApi thirdPartyApi;
    private Map<Integer, IRateLimiter> perEntityRateLimiters;

    MyBackendImpl(RateLimiterArgs userLimitArgs, RateLimiterArgs perEntityLimitArgs, ThirdPartyApi thirdPartyApi) {
        this.userRateLimiter = RateLimiterFactory.getRateLimiter(userLimitArgs.limit, userLimitArgs.period, userLimitArgs.timeUnit, RateLimiterType.SEMAPHORE_BASED);
        this.perEntityLimitArgs = perEntityLimitArgs;
        this.thirdPartyApi = thirdPartyApi;
        this.perEntityRateLimiters = new ConcurrentHashMap<Integer, IRateLimiter>();
    }

    IRateLimiter getperEntityRateLimiter(int perEntityId) {
        if (perEntityRateLimiters.containsKey(perEntityId)) {
            return perEntityRateLimiters.get(perEntityId);
        }
        perEntityRateLimiters.put(perEntityId, RateLimiterFactory.getRateLimiter(perEntityLimitArgs.limit, perEntityLimitArgs.period, perEntityLimitArgs.timeUnit, RateLimiterType.SEMAPHORE_BASED));
        return perEntityRateLimiters.get(perEntityId);
    }


    @Override
    public void fetchSystemEventsData(int perEntityId) {
        IRateLimiter perEntityRateLimiter = getperEntityRateLimiter(perEntityId);

        ThrottleResponse userLimitThrottleResponse = userRateLimiter.throttle();
        ThrottleResponse perEntityLimitThrottleResponse = perEntityRateLimiter.throttle();

        if (userLimitThrottleResponse.isAllowed()) {
            if (!perEntityLimitThrottleResponse.isAllowed()) {
                userRateLimiter.release();
                System.out.printf("[ThirdPartyApi] [GetSystemEventsData] perEntity rate limit exceeded for perEntity:%s. Wait time: %s\n", perEntityId, perEntityLimitThrottleResponse.getWaitTimeMillis());
            } else {
                boolean data = thirdPartyApi.getSafetyEventsData(perEntityId);
                System.out.printf("Fetched data for perEntity: %s, Data: %s\n", perEntityId, data);
            }
        } else {
            System.out.printf("[ThirdPartyApi] [GetSystemEventsData] User rate limit exceeded for perEntity:%s. Wait time: %s\n", perEntityId, userLimitThrottleResponse.getWaitTimeMillis());
        }
    }

    @Override
    public void shutdown() {
        userRateLimiter.shutdown();
        perEntityRateLimiters.values().stream().forEach(IRateLimiter::shutdown);
    }
}
