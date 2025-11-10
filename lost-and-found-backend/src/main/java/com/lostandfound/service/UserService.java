package com.lostandfound.service;

import com.lostandfound.dto.request.LoginRequest;
import com.lostandfound.dto.request.RegisterRequest;
import com.lostandfound.dto.response.AuthResponse;
import com.lostandfound.dto.response.TokenRefreshResponse;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.model.RefreshToken;
import com.lostandfound.model.User;
import com.lostandfound.repository.UserRepository;
import com.lostandfound.security.JwtTokenProvider;
import com.lostandfound.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.expiration}")
    private Long jwtExpirationMs;

    @Transactional
    public AuthResponse registerUser(RegisterRequest request, String ipAddress, String userAgent) {
        // Normalize email to lowercase
        String email = request.getEmail().toLowerCase().trim();

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            logger.warn("Registration attempt with existing email: {}", email);
            throw new BadRequestException("Email address is already registered");
        }

        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // Create new user
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.USER);

        try {
            user = userRepository.save(user);
            logger.info("New user registered successfully: {}", email);
        } catch (Exception e) {
            logger.error("Error during user registration: {}", email, e);
            throw new BadRequestException("Registration failed. Please try again.");
        }

        // Auto-login after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate access token
        String accessToken = tokenProvider.generateToken(authentication);

        // Generate refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                user.getId(), ipAddress, userAgent
        );

        // Build user DTO
        AuthResponse.UserDTO userDTO = AuthResponse.UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();

        return AuthResponse.builder()
                .success(true)
                .message("Registration successful")
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000) // Convert to seconds
                .user(userDTO)
                .build();
    }

    @Transactional
    public AuthResponse loginUser(LoginRequest request, String ipAddress, String userAgent) {
        String email = request.getEmail().toLowerCase().trim();

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate access token
            String accessToken = tokenProvider.generateToken(authentication);

            // Fetch user details
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BadRequestException("User not found"));

            // Generate refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                    user.getId(), ipAddress, userAgent
            );

            logger.info("User logged in successfully: {}", email);

            // Build user DTO
            AuthResponse.UserDTO userDTO = AuthResponse.UserDTO.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();

            return AuthResponse.builder()
                    .success(true)
                    .message("Login successful")
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtExpirationMs / 1000) // Convert to seconds
                    .user(userDTO)
                    .build();

        } catch (BadCredentialsException e) {
            logger.warn("Failed login attempt for email: {}", email);
            throw new BadCredentialsException("Invalid email or password");
        } catch (LockedException e) {
            logger.warn("Login attempt for locked account: {}", email);
            throw new BadRequestException("Account is locked. Please contact support.");
        } catch (Exception e) {
            logger.error("Error during login for email: {}", email, e);
            throw new BadRequestException("Login failed. Please try again.");
        }
    }

    @Transactional
    public void logoutUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Delete all refresh tokens for this user
        refreshTokenService.revokeAllUserTokens(userId);

        logger.info("User logged out successfully: {}", user.getEmail());
    }

    @Transactional
    public TokenRefreshResponse refreshToken(String refreshTokenStr) {
        return refreshTokenService.findByToken(refreshTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    UserPrincipal userPrincipal = UserPrincipal.create(user);
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            userPrincipal, null, userPrincipal.getAuthorities()
                    );

                    String newAccessToken = tokenProvider.generateToken(auth);

                    return TokenRefreshResponse.builder()
                            .success(true)
                            .message("Token refreshed successfully")
                            .accessToken(newAccessToken)
                            .refreshToken(refreshTokenStr)
                            .tokenType("Bearer")
                            .build();
                })
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new BadRequestException("User not found"));
    }

    @Transactional
    public void deleteUser(Long userId, UserPrincipal currentUser) {
        // Verify current user is admin
        User adminUser = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new BadRequestException("Admin user not found"));

        if (adminUser.getRole() != User.Role.ADMIN) {
            throw new BadRequestException("Only admins can delete users");
        }

        // Find the user to delete
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Prevent deleting admin users
        if (userToDelete.getRole() == User.Role.ADMIN) {
            throw new BadRequestException("Cannot delete admin user");
        }

        logger.info("Admin {} deleting user {}", adminUser.getEmail(), userToDelete.getEmail());

        // Delete all refresh tokens for this user
        refreshTokenService.revokeAllUserTokens(userId);

        // Delete the user (cascading will handle related data if configured)
        userRepository.delete(userToDelete);

        logger.info("Successfully deleted user: {}", userToDelete.getEmail());
    }
}