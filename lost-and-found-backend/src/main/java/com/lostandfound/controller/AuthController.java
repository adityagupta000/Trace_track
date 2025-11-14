package com.lostandfound.controller;

import com.lostandfound.dto.request.LoginRequest;
import com.lostandfound.dto.request.RegisterRequest;
import com.lostandfound.dto.response.ApiResponse;
import com.lostandfound.dto.response.AuthResponse;
import com.lostandfound.dto.response.TokenRefreshResponse;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.security.UserPrincipal;
import com.lostandfound.service.RefreshTokenService;
import com.lostandfound.service.UserService;
import com.lostandfound.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponse response = userService.registerUser(request, ipAddress, userAgent);

        // CRITICAL: Set BOTH access and refresh tokens in cookies
        cookieUtil.addAccessTokenCookie(httpResponse, response.getAccessToken());
        cookieUtil.addRefreshTokenCookie(httpResponse, response.getRefreshToken());

        // Remove tokens from response body (security best practice)
        response.setAccessToken(null);
        response.setRefreshToken(null);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponse response = userService.loginUser(request, ipAddress, userAgent);

        // CRITICAL: Set BOTH access and refresh tokens in cookies
        cookieUtil.addAccessTokenCookie(httpResponse, response.getAccessToken());
        cookieUtil.addRefreshTokenCookie(httpResponse, response.getRefreshToken());

        // Remove tokens from response body
        response.setAccessToken(null);
        response.setRefreshToken(null);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        // Get refresh token from cookie
        String refreshToken = cookieUtil.getRefreshToken(httpRequest)
                .orElseThrow(() -> new BadRequestException("Refresh token not found"));

        TokenRefreshResponse response = userService.refreshToken(refreshToken);

        // Update BOTH access and refresh token cookies
        cookieUtil.addAccessTokenCookie(httpResponse, response.getAccessToken());
        cookieUtil.addRefreshTokenCookie(httpResponse, response.getRefreshToken());

        // Remove tokens from response body
        response.setAccessToken(null);
        response.setRefreshToken(null);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        if (currentUser == null) {
            throw new BadRequestException("You are not logged in");
        }

        // Get refresh token from cookie
        String refreshToken = cookieUtil.getRefreshToken(httpRequest).orElse(null);

        if (refreshToken != null) {
            refreshTokenService.revokeTokenForUser(refreshToken, currentUser.getId());
        } else {
            refreshTokenService.revokeAllUserTokens(currentUser.getId());
        }

        // Clear all auth cookies
        cookieUtil.deleteAllAuthCookies(httpResponse);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Logged out successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse> validateToken(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (currentUser == null) {
            throw new BadRequestException("Invalid or expired token");
        }

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Token is valid")
                .data(currentUser.getEmail())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (currentUser == null) {
            throw new BadRequestException("Not authenticated");
        }

        AuthResponse.UserDTO userDTO = AuthResponse.UserDTO.builder()
                .id(currentUser.getId())
                .name(currentUser.getName())
                .email(currentUser.getEmail())
                .role(currentUser.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""))
                .build();

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("User details retrieved successfully")
                .data(userDTO)
                .build();

        return ResponseEntity.ok(response);
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
        return ip.matches(ipv4Pattern);
    }
}