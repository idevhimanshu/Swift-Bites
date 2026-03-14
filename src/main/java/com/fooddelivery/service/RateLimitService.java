package com.fooddelivery.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    @Value("${rate-limit.login.capacity:5}")
    private int loginCapacity;

    @Value("${rate-limit.api.capacity:100}")
    private int apiCapacity;

    private final Map<String, BucketState> buckets = new ConcurrentHashMap<>();

    public boolean isAllowed(String key, boolean isLogin) {
        int capacity = isLogin ? loginCapacity : apiCapacity;
        long windowMs = 60_000L;

        BucketState state = buckets.compute(key, (k, v) -> {
            long now = Instant.now().toEpochMilli();
            if (v == null || (now - v.windowStart) >= windowMs) {
                return new BucketState(now, 1);
            }
            v.count++;
            return v;
        });
        return state.count <= capacity;
    }

    private static class BucketState {
        long windowStart;
        int count;
        BucketState(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
