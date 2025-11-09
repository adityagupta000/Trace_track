package com.lostandfound.service;

import com.lostandfound.dto.response.ApiResponse;
import com.lostandfound.dto.response.ClaimResponse;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.exception.UnauthorizedException;
import com.lostandfound.model.Claim;
import com.lostandfound.model.Item;
import com.lostandfound.model.User;
import com.lostandfound.repository.ClaimRepository;
import com.lostandfound.repository.ItemRepository;
import com.lostandfound.repository.UserRepository;
import com.lostandfound.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private static final Logger logger = LoggerFactory.getLogger(ClaimService.class);

    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    public ApiResponse claimItem(Long itemId, UserPrincipal currentUser) {
        // Validate user is authenticated
        if (currentUser == null) {
            throw new UnauthorizedException("You must be logged in to claim an item");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        // Check if item is already claimed
        if (item.getStatus() == Item.Status.CLAIMED) {
            throw new BadRequestException("This item has already been claimed by someone else");
        }

        // Check if user is trying to claim their own item
        if (item.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You cannot claim your own item");
        }

        // Check if user has already claimed this item
        if (claimRepository.existsByItemAndClaimedBy(item, user)) {
            throw new BadRequestException("You have already submitted a claim for this item");
        }

        // Create new claim
        Claim claim = new Claim();
        claim.setItem(item);
        claim.setClaimedBy(user);
        claim.setClaimantName(user.getName());
        claim.setClaimantEmail(user.getEmail());

        claimRepository.save(claim);

        // Update item status
        item.setStatus(Item.Status.CLAIMED);
        itemRepository.save(item);

        logger.info("User {} claimed item {}", user.getEmail(), itemId);

        return ApiResponse.builder()
                .success(true)
                .message("Item claimed successfully. The item owner will be notified.")
                .build();
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getUserClaims(UserPrincipal currentUser) {
        if (currentUser == null) {
            // For admin use - return all claims
            List<Claim> claims = claimRepository.findAllByOrderByClaimedAtDesc();
            return claims.stream()
                    .map(this::mapToClaimResponse)
                    .collect(Collectors.toList());
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        List<Claim> claims = claimRepository.findByClaimedByOrderByClaimedAtDesc(user);
        return claims.stream()
                .map(this::mapToClaimResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteClaim(Long claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", claimId));

        Item item = claim.getItem();

        claimRepository.delete(claim);

        // Update item status back to FOUND if it was CLAIMED and no other claims exist
        if (item.getStatus() == Item.Status.CLAIMED) {
            List<Claim> remainingClaims = claimRepository.findByItem(item);
            if (remainingClaims.isEmpty()) {
                item.setStatus(Item.Status.FOUND);
                itemRepository.save(item);
            }
        }

        logger.info("Deleted claim {} for item {}", claimId, item.getId());
    }

    @Transactional
    public void deleteClaimByUser(Long claimId, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("You must be logged in to delete a claim");
        }

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", claimId));

        // Check if user owns this claim or is admin
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        boolean isOwner = claim.getClaimedBy().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedException("You don't have permission to delete this claim");
        }

        deleteClaim(claimId);
    }

    private ClaimResponse mapToClaimResponse(Claim claim) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .itemId(claim.getItem().getId())
                .itemName(claim.getItem().getName())
                .description(claim.getItem().getDescription())
                .location(claim.getItem().getLocation())
                .claimedBy(claim.getClaimedBy().getId())
                .claimerName(claim.getClaimedBy().getName())
                .claimantName(claim.getClaimantName())
                .claimantEmail(claim.getClaimantEmail())
                .claimedAt(claim.getClaimedAt())
                .build();
    }
}