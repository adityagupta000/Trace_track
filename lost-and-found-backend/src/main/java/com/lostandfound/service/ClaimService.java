package com.lostandfound.service;

import com.lostandfound.dto.response.ApiResponse;
import com.lostandfound.dto.response.ClaimResponse;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.model.Claim;
import com.lostandfound.model.Item;
import com.lostandfound.model.User;
import com.lostandfound.repository.ClaimRepository;
import com.lostandfound.repository.ItemRepository;
import com.lostandfound.repository.UserRepository;
import com.lostandfound.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaimService {
    
    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public ApiResponse claimItem(Long itemId, UserPrincipal currentUser) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));
        
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
        
        if (item.getStatus() == Item.Status.CLAIMED) {
            throw new BadRequestException("Item already claimed");
        }
        
        if (claimRepository.existsByItemAndClaimedBy(item, user)) {
            throw new BadRequestException("You have already claimed this item");
        }
        
        Claim claim = new Claim();
        claim.setItem(item);
        claim.setClaimedBy(user);
        claim.setClaimantName(user.getName());
        claim.setClaimantEmail(user.getEmail());
        
        claimRepository.save(claim);
        
        // Update item status
        item.setStatus(Item.Status.CLAIMED);
        itemRepository.save(item);
        
        return ApiResponse.builder()
                .success(true)
                .message("Item claimed successfully")
                .build();
    }
    
    @Transactional(readOnly = true)
    public List<ClaimResponse> getUserClaims(UserPrincipal currentUser) {
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
        
        // Update item status back to FOUND if it was CLAIMED
        if (item.getStatus() == Item.Status.CLAIMED) {
            item.setStatus(Item.Status.FOUND);
            itemRepository.save(item);
        }
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