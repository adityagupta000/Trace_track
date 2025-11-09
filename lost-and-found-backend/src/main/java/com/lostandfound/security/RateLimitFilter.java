package com.lostandfound.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lostandfound.config.RateLimitConfig;
import com.lostandfound.dto.response.ApiResponse;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper;

    @Value("${rate.limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip rate limiting if disabled
        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String clientIp = getClientIP(request);
        
        // Determine rate limit type based on path
        RateLimitConfig.RateLimitType limitType = determineRateLimitType(path);
        
        // Create unique key: IP + Path pattern
        String bucketKey = clientIp + ":" + limitType.name();
        
        // Get or create bucket for this key
        Bucket bucket = rateLimitConfig.resolveBucket(bucketKey, limitType);
        
        // Try to consume a token
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            // Request allowed - add rate limit headers
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", 
                    String.valueOf(limitType.getRefillDuration().getSeconds()));
            
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            
            logger.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            
            ApiResponse apiResponse = ApiResponse.builder()
                    .success(false)
                    .message("Rate limit exceeded. Please try again in " + waitForRefill + " seconds.")
                    .build();
            
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        }
    }

    /**
     * Determine rate limit type based on request path
     */
    private RateLimitConfig.RateLimitType determineRateLimitType(String path) {
        if (path.startsWith("/api/auth/")) {
            return RateLimitConfig.RateLimitType.AUTH;
        } else if (path.startsWith("/admin/")) {
            return RateLimitConfig.RateLimitType.ADMIN;
        } else if (path.startsWith("/items") && path.contains("/image")) {
            return RateLimitConfig.RateLimitType.UPLOAD;
        } else if (path.startsWith("/uploads/") || path.startsWith("/static/")) {
            return RateLimitConfig.RateLimitType.PUBLIC;
        } else {
            return RateLimitConfig.RateLimitType.API;
        }
    }

    /**
     * Extract client IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        // X-Forwarded-For can contain multiple IPs, get the first one
        return xfHeader.split(",")[0].trim();
    }

    /**
     * Skip rate limiting for health check endpoints
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health") || 
               path.startsWith("/actuator/info");
    }
}