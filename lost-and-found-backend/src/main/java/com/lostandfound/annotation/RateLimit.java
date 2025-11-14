package com.lostandfound.annotation;

import com.lostandfound.config.RateLimitConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    RateLimitConfig.RateLimitType type() default RateLimitConfig.RateLimitType.API;
}