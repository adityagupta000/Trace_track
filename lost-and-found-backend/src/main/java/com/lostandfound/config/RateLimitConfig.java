package com.lostandfound.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Rate limit configurations - INCREASED for development
     */
    public enum RateLimitType {
        // Auth endpoints - Increased from 5 to 50 per minute
        AUTH(50, Duration.ofMinutes(1)),

        // General API endpoints - Increased to 500 per minute
        API(500, Duration.ofMinutes(1)),

        // Admin endpoints - Increased to 200 per minute
        ADMIN(200, Duration.ofMinutes(1)),

        // File upload - Increased to 50 per 5 minutes
        UPLOAD(50, Duration.ofMinutes(5)),

        // Public endpoints - Increased to 1000 per minute
        PUBLIC(1000, Duration.ofMinutes(1));

        private final long capacity;
        private final Duration refillDuration;

        RateLimitType(long capacity, Duration refillDuration) {
            this.capacity = capacity;
            this.refillDuration = refillDuration;
        }

        public long getCapacity() {
            return capacity;
        }

        public Duration getRefillDuration() {
            return refillDuration;
        }
    }

    public Bucket resolveBucket(String key, RateLimitType limitType) {
        return cache.computeIfAbsent(key, k -> createNewBucket(limitType));
    }

    private Bucket createNewBucket(RateLimitType limitType) {
        Bandwidth limit = Bandwidth.classic(
            limitType.getCapacity(),
            Refill.intervally(limitType.getCapacity(), limitType.getRefillDuration())
        );
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    public void clearBucket(String key) {
        cache.remove(key);
    }

    public void clearAllBuckets() {
        cache.clear();
    }

    public int getCacheSize() {
        return cache.size();
    }
}