package com.lostandfound.aspect;

import com.lostandfound.annotation.RateLimit;
import com.lostandfound.config.RateLimitConfig;
import com.lostandfound.exception.BadRequestException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP Aspect for method-level rate limiting using @RateLimit annotation
 * This is optional and works alongside the filter-based rate limiting
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);

    private final RateLimitConfig rateLimitConfig;

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = getCurrentRequest();

        if (request == null) {
            // If no request context, skip rate limiting
            return joinPoint.proceed();
        }

        String clientIp = getClientIP(request);
        String method = joinPoint.getSignature().getName();

        RateLimitConfig.RateLimitType limitType = rateLimit.type();
        String bucketKey = clientIp + ":" + method + ":" + limitType.name();

        Bucket bucket = rateLimitConfig.resolveBucket(bucketKey, limitType);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            logger.debug("Rate limit check passed for IP: {} on method: {}", clientIp, method);
            return joinPoint.proceed();
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            logger.warn("Rate limit exceeded for IP: {} on method: {}", clientIp, method);
            throw new BadRequestException(
                    "Rate limit exceeded. Please try again in " + waitForRefill + " seconds."
            );
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        String remoteAddr = request.getRemoteAddr();
        boolean isTrustedProxy = isTrustedProxy(remoteAddr);

        if (isTrustedProxy && xfHeader != null && !xfHeader.isEmpty()) {
            String clientIp = xfHeader.split(",")[0].trim();
            if (isValidIP(clientIp)) {
                return clientIp;
            }
        }

        return remoteAddr;
    }

    private boolean isTrustedProxy(String ip) {
        return "127.0.0.1".equals(ip) ||
                "0:0:0:0:0:0:0:1".equals(ip) ||
                "::1".equals(ip);
    }

    private boolean isValidIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        String ipv4Pattern = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";
        String ipv6Pattern = "^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2})$";
        return ip.matches(ipv4Pattern) || ip.matches(ipv6Pattern);
    }
}