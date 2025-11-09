package com.lostandfound.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    /**
     * In-memory cache for storing buckets per IP address
     * For production, consider using Redis or Hazelcast for distributed caching
     */
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Rate limit configurations for different endpoints
     */
    public enum RateLimitType {
        // Auth endpoints - 5 requests per minute
        AUTH(5, Duration.ofMinutes(1)),
        
        // General API endpoints - 100 requests per minute
        API(100, Duration.ofMinutes(1)),
        
        // Admin endpoints - 50 requests per minute
        ADMIN(50, Duration.ofMinutes(1)),
        
        // File upload - 10 requests per 5 minutes
        UPLOAD(10, Duration.ofMinutes(5)),
        
        // Public endpoints - 200 requests per minute
        PUBLIC(200, Duration.ofMinutes(1));

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

    /**
     * Resolve bucket for given key and rate limit type
     */
    public Bucket resolveBucket(String key, RateLimitType limitType) {
        return cache.computeIfAbsent(key, k -> createNewBucket(limitType));
    }

    /**
     * Create new bucket with specified rate limit
     */
    private Bucket createNewBucket(RateLimitType limitType) {
        Bandwidth limit = Bandwidth.classic(
                limitType.getCapacity(),
                Refill.intervally(limitType.getCapacity(), limitType.getRefillDuration())
        );
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Clear bucket for specific key (useful for testing or admin actions)
     */
    public void clearBucket(String key) {
        cache.remove(key);
    }

    /**
     * Clear all buckets (useful for testing)
     */
    public void clearAllBuckets() {
        cache.clear();
    }

    /**
     * Get current cache size
     */
    public int getCacheSize() {
        return cache.size();
    }
}