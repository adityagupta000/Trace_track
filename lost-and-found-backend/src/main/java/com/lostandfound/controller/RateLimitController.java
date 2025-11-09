package com.lostandfound.controller;

import com.lostandfound.config.RateLimitConfig;
import com.lostandfound.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/rate-limit")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class RateLimitController {

    private final RateLimitConfig rateLimitConfig;

    /**
     * Get rate limit statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getRateLimitStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeBuckets", rateLimitConfig.getCacheSize());
        stats.put("rateLimitTypes", getRateLimitInfo());

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Rate limit statistics")
                .data(stats)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Clear rate limit for specific IP
     */
    @DeleteMapping("/clear/{ip}")
    public ResponseEntity<ApiResponse> clearRateLimitForIp(@PathVariable String ip) {
        // Clear all buckets for this IP across all limit types
        for (RateLimitConfig.RateLimitType type : RateLimitConfig.RateLimitType.values()) {
            String key = ip + ":" + type.name();
            rateLimitConfig.clearBucket(key);
        }

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Rate limit cleared for IP: " + ip)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Clear all rate limits (use with caution)
     */
    @DeleteMapping("/clear-all")
    public ResponseEntity<ApiResponse> clearAllRateLimits() {
        rateLimitConfig.clearAllBuckets();

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("All rate limits cleared")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get rate limit configuration info
     */
    private Map<String, Object> getRateLimitInfo() {
        Map<String, Object> info = new HashMap<>();
        
        for (RateLimitConfig.RateLimitType type : RateLimitConfig.RateLimitType.values()) {
            Map<String, Object> typeInfo = new HashMap<>();
            typeInfo.put("capacity", type.getCapacity());
            typeInfo.put("refillDurationSeconds", type.getRefillDuration().getSeconds());
            info.put(type.name(), typeInfo);
        }
        
        return info;
    }
}