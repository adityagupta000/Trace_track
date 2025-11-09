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
import jakarta.servlet.http.HttpServletRequest;
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

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponse response = userService.registerUser(request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponse response = userService.loginUser(request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Validate that refresh token is provided
        if (request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
            throw new BadRequestException("Refresh token is required");
        }

        TokenRefreshResponse response = userService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody(required = false) TokenRefreshRequest request) {

        // User must be authenticated to logout
        if (currentUser == null) {
            throw new BadRequestException("You are not logged in");
        }

        // Validate and revoke refresh token if provided
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().trim().isEmpty()) {
            // Verify the token belongs to the current user before revoking
            refreshTokenService.revokeTokenForUser(request.getRefreshToken(), currentUser.getId());
        } else {
            // Revoke all tokens for this user
            refreshTokenService.revokeAllUserTokens(currentUser.getId());
        }

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
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}