/**
 * Author: Harsh Gupta
 * Data: 2022-11-14
 * Contact: +91-8299828697
 */

package com.backend.ratelimiter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final RateLimiterArgs USER_LIMIT_ARGS = new RateLimiterArgs(1, 1, TimeUnit.MILLISECONDS);
    private static final RateLimiterArgs perEntity_LIMIT_ARGS = new RateLimiterArgs(1, 1, TimeUnit.MILLISECONDS);

    public static void main(String[] args) {
        System.out.println("Testing the rate limiter");
        MyBackend myBackend = new MyBackendImpl(USER_LIMIT_ARGS, perEntity_LIMIT_ARGS, new MockThirdPartyApiImpl());

        ExecutorService pool = Executors.newFixedThreadPool(10);
        pool.execute(() -> {
            myBackend.fetchSystemEventsData(1);
        });pool.execute(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            myBackend.fetchSystemEventsData(2);
        });pool.execute(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            myBackend.fetchSystemEventsData(3);
        });pool.execute(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            myBackend.fetchSystemEventsData(4);
        });pool.execute(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            myBackend.fetchSystemEventsData(2);
        });pool.execute(() -> {
            myBackend.fetchSystemEventsData(1);
        });pool.execute(() -> {
            myBackend.fetchSystemEventsData(5);
        });

        try {
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        myBackend.shutdown();
    }
}