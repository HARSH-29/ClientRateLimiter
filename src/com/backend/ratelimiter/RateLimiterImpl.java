package com.backend.ratelimiter;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RateLimiterImpl implements IRateLimiter {
    private static final int THREAD_POOL_SIZE = 1;
    private static final int ONE_SECOND_IN_MILLIS = 1000;
    private static final int TIME_PERIOD_DIVIDER = 100;
    private final int limit;
    private final int period;
    private final TimeUnit timeUnit;
    private final Semaphore semaphore;
    private final ScheduledExecutorService executorService;
    private AtomicBoolean firstAcquire;
    private Set<Integer> requestTimePeriods;
    private ScheduledFuture<?> task = null;

    RateLimiterImpl(int limit, int period, TimeUnit timeUnit) {
        this.limit = limit;
        this.period = period;
        this.timeUnit = timeUnit;
        this.semaphore = new Semaphore(limit);
        this.executorService = new ScheduledThreadPoolExecutor(THREAD_POOL_SIZE);
        this.firstAcquire = new AtomicBoolean(false);
        this.requestTimePeriods = new ConcurrentSkipListSet<Integer>();
    }

    @Override
    public ThrottleResponse throttle() {
        if (semaphore.tryAcquire()) {
            afterAcquirePermit();
            return new ThrottleResponse(true);
        }
        ThrottleResponse response = new ThrottleResponse(false);
        response.setWaitTimeMillis(getWaitingTimeMillis());
        return response;
    }

    @Override
    public void release() {
        // Does not remove the request time from timeperiod windows?
        semaphore.release();
    }

    private boolean getFirstAcquire() {
        return this.firstAcquire.get();
    }

    private void setFirstAcquire() {
        this.firstAcquire.compareAndSet(false, true);
    }

    private void afterAcquirePermit() {
        if (!getFirstAcquire()) {
            // If the rate limiter is invoked the very first time.
            setFirstAcquire();
            // Schedules to run releasePermit at fixed intervals starting
            // from period, then 2*period, 3*period ... and so on.
            this.task = executorService.scheduleAtFixedRate(this::endOfPeriod, period, period, timeUnit);
        }
        addRequestTimePeriod((int) (System.currentTimeMillis()%TIME_PERIOD_DIVIDER));
    }

    private void addRequestTimePeriod(int timePeriod) {
        this.requestTimePeriods.add(timePeriod);
    }

    private int getWaitingTimeMillis() {
        // 1000 - 400 = 600
        Optional<Integer> oldestRequestTimestampInLastPeriod = this.requestTimePeriods.stream().sorted().findFirst();
        if (oldestRequestTimestampInLastPeriod.isEmpty())
            return convertToMillis(period, timeUnit);
        return convertToMillis(ONE_SECOND_IN_MILLIS-oldestRequestTimestampInLastPeriod.get()*TIME_PERIOD_DIVIDER, TimeUnit.MILLISECONDS);
    }

    private void clearRequestTimePeriods() {
        this.requestTimePeriods.clear();
    }
    private void releaseAllPermits() {
        semaphore.release(limit);
    }

    private void endOfPeriod() {
        releaseAllPermits();
        clearRequestTimePeriods();
    }

    private static int convertToMillis(int time, TimeUnit timeUnit) {
        if (timeUnit == TimeUnit.MILLISECONDS) return time;
        if (timeUnit == TimeUnit.SECONDS) return time * ONE_SECOND_IN_MILLIS;
        if (timeUnit == TimeUnit.MINUTES) return time * 60 * ONE_SECOND_IN_MILLIS;
        return time;
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
        task.cancel(false);
    }
}
