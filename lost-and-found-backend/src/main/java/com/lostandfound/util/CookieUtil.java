package com.lostandfound.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {

    @Value("${cookie.domain:localhost}")
    private String cookieDomain;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${cookie.same-site:Lax}")
    private String cookieSameSite;

    // Cookie names
    public static final String ACCESS_TOKEN_COOKIE = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    // Cookie max ages (in seconds)
    private static final int ACCESS_TOKEN_MAX_AGE = 15 * 60; // 15 minutes
    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60; // 7 days

    /**
     * Add access token cookie
     */
    public void addAccessTokenCookie(HttpServletResponse response, String token) {
        addCookie(response, ACCESS_TOKEN_COOKIE, token, ACCESS_TOKEN_MAX_AGE, true);
    }

    /**
     * Add refresh token cookie
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        addCookie(response, REFRESH_TOKEN_COOKIE, token, REFRESH_TOKEN_MAX_AGE, true);
    }

    /**
     * Generic method to add cookie with proper SameSite attribute
     */
    private void addCookie(HttpServletResponse response, String name, String value,
                           int maxAge, boolean httpOnly) {

        // Build cookie header manually to support SameSite
        StringBuilder cookieHeader = new StringBuilder();
        cookieHeader.append(name).append("=").append(value);
        cookieHeader.append("; Path=/");
        cookieHeader.append("; Max-Age=").append(maxAge);

        if (httpOnly) {
            cookieHeader.append("; HttpOnly");
        }

        if (cookieSecure) {
            cookieHeader.append("; Secure");
        }

        // Add SameSite attribute
        if (cookieSameSite != null && !cookieSameSite.isEmpty()) {
            cookieHeader.append("; SameSite=").append(cookieSameSite);
        }

        // Set the cookie
        response.addHeader("Set-Cookie", cookieHeader.toString());
    }

    /**
     * Get cookie value by name
     */
    public Optional<String> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    /**
     * Get access token from cookie
     */
    public Optional<String> getAccessToken(HttpServletRequest request) {
        return getCookie(request, ACCESS_TOKEN_COOKIE);
    }

    /**
     * Get refresh token from cookie
     */
    public Optional<String> getRefreshToken(HttpServletRequest request) {
        return getCookie(request, REFRESH_TOKEN_COOKIE);
    }

    /**
     * Delete cookie
     */
    public void deleteCookie(HttpServletResponse response, String name) {
        StringBuilder cookieHeader = new StringBuilder();
        cookieHeader.append(name).append("=");
        cookieHeader.append("; Path=/");
        cookieHeader.append("; Max-Age=0");
        cookieHeader.append("; HttpOnly");

        if (cookieSecure) {
            cookieHeader.append("; Secure");
        }

        if (cookieSameSite != null && !cookieSameSite.isEmpty()) {
            cookieHeader.append("; SameSite=").append(cookieSameSite);
        }

        response.addHeader("Set-Cookie", cookieHeader.toString());
    }

    /**
     * Delete access token cookie
     */
    public void deleteAccessToken(HttpServletResponse response) {
        deleteCookie(response, ACCESS_TOKEN_COOKIE);
    }

    /**
     * Delete refresh token cookie
     */
    public void deleteRefreshToken(HttpServletResponse response) {
        deleteCookie(response, REFRESH_TOKEN_COOKIE);
    }

    /**
     * Delete all auth cookies
     */
    public void deleteAllAuthCookies(HttpServletResponse response) {
        deleteAccessToken(response);
        deleteRefreshToken(response);
    }
}