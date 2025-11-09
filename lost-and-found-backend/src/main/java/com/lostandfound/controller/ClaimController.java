package com.lostandfound.controller;

import com.lostandfound.dto.response.ApiResponse;
import com.lostandfound.security.UserPrincipal;
import com.lostandfound.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ClaimController {
    
    private final ClaimService claimService;
    
    @PostMapping("/{itemId}/claim")
    public ResponseEntity<ApiResponse> claimItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        ApiResponse response = claimService.claimItem(itemId, currentUser);
        return ResponseEntity.ok(response);
    }
}