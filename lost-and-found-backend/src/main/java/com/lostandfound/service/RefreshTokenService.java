package com.lostandfound.service;

import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.UnauthorizedException;
import com.lostandfound.model.RefreshToken;
import com.lostandfound.model.User;
import com.lostandfound.repository.RefreshTokenRepository;
import com.lostandfound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenDurationMs;

    @Transactional
    public RefreshToken createRefreshToken(Long userId, String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Delete old refresh tokens for this user
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        logger.info("Created new refresh token for user: {}", user.getEmail());
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }
        return refreshTokenRepository.findByToken(token.trim());
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            logger.warn("Refresh token expired for user: {}", token.getUser().getEmail());
            throw new BadRequestException("Refresh token expired. Please login again.");
        }

        if (token.isRevoked()) {
            logger.warn("Attempted to use revoked refresh token for user: {}", token.getUser().getEmail());
            throw new BadRequestException("Refresh token has been revoked. Please login again.");
        }

        return token;
    }

    @Transactional
    public void revokeToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new BadRequestException("Invalid refresh token");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token.trim())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new BadRequestException("Token is already revoked");
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        logger.info("Revoked refresh token for user: {}", refreshToken.getUser().getEmail());
    }

    @Transactional
    public void revokeTokenForUser(String token, Long userId) {
        if (token == null || token.trim().isEmpty()) {
            throw new BadRequestException("Invalid refresh token");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token.trim())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        // Verify the token belongs to the user
        if (!refreshToken.getUser().getId().equals(userId)) {
            logger.warn("User {} attempted to revoke token belonging to user {}",
                    userId, refreshToken.getUser().getId());
            throw new UnauthorizedException("This token does not belong to you");
        }

        if (refreshToken.isRevoked()) {
            throw new BadRequestException("Token is already revoked");
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        logger.info("Revoked refresh token for user: {}", refreshToken.getUser().getEmail());
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        refreshTokenRepository.deleteByUser(user);
        logger.info("Revoked all refresh tokens for user: {}", user.getEmail());
    }

    @Transactional
    public void deleteExpiredTokens() {
        int deletedCount = refreshTokenRepository.deleteAllExpiredTokens(Instant.now());
        logger.info("Deleted {} expired refresh tokens", deletedCount);
    }
}