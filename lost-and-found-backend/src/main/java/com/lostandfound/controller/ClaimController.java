package com.lostandfound.controller;

import com.lostandfound.dto.response.ApiResponse;
import com.lostandfound.dto.response.ClaimResponse;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.security.UserPrincipal;
import com.lostandfound.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping("/item/{itemId}")
    public ResponseEntity<ApiResponse> claimItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (currentUser == null) {
            throw new BadRequestException("You must be logged in to claim an item");
        }

        ApiResponse response = claimService.claimItem(itemId, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyClaims(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (currentUser == null) {
            throw new BadRequestException("You must be logged in to view your claims");
        }

        List<ClaimResponse> claims = claimService.getUserClaims(currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Claims retrieved successfully");
        response.put("claims", claims);
        response.put("count", claims.size());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{claimId}")
    public ResponseEntity<ApiResponse> deleteClaim(
            @PathVariable Long claimId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (currentUser == null) {
            throw new BadRequestException("You must be logged in to delete a claim");
        }

        claimService.deleteClaimByUser(claimId, currentUser);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Claim deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }
}