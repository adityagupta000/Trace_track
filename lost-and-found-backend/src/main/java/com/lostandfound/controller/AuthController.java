package com.lostandfound.controller;

import com.lostandfound.dto.request.LoginRequest;
import com.lostandfound.dto.request.RegisterRequest;
import com.lostandfound.dto.request.TokenRefreshRequest;
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
    private final CookieUtil cookieUtil; // Add this

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) { // Add response parameter

        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponse response = userService.registerUser(request, ipAddress, userAgent);

        // Set refresh token in httpOnly cookie
        cookieUtil.addRefreshTokenCookie(httpResponse, response.getRefreshToken());

        // Remove refresh token from response body (security best practice)
        response.setRefreshToken(null);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) { // Add response parameter

        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponse response = userService.loginUser(request, ipAddress, userAgent);

        // Set refresh token in httpOnly cookie
        cookieUtil.addRefreshTokenCookie(httpResponse, response.getRefreshToken());

        // Remove refresh token from response body
        response.setRefreshToken(null);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        // Get refresh token from cookie instead of request body
        String refreshToken = cookieUtil.getRefreshToken(httpRequest)
                .orElseThrow(() -> new BadRequestException("Refresh token not found"));

        TokenRefreshResponse response = userService.refreshToken(refreshToken);

        // Update refresh token cookie
        cookieUtil.addRefreshTokenCookie(httpResponse, response.getRefreshToken());

        // Remove refresh token from response body
        response.setRefreshToken(null);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        // User must be authenticated to logout
        if (currentUser == null) {
            throw new BadRequestException("You are not logged in");
        }

        // Get refresh token from cookie
        String refreshToken = cookieUtil.getRefreshToken(httpRequest).orElse(null);

        if (refreshToken != null) {
            // Verify the token belongs to the current user before revoking
            refreshTokenService.revokeTokenForUser(refreshToken, currentUser.getId());
        } else {
            // Revoke all tokens for this user
            refreshTokenService.revokeAllUserTokens(currentUser.getId());
        }

        // Clear auth cookies
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