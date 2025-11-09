package com.lostandfound.controller;

import com.lostandfound.dto.response.ClaimResponse;
import com.lostandfound.dto.response.DashboardResponse;
import com.lostandfound.dto.response.ItemResponse;
import com.lostandfound.dto.response.MessageResponse;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.security.UserPrincipal;
import com.lostandfound.service.ClaimService;
import com.lostandfound.service.ItemService;
import com.lostandfound.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ItemService itemService;
    private final ClaimService claimService;
    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Validate user is authenticated
        if (currentUser == null) {
            throw new BadRequestException("You must be logged in to access the dashboard");
        }

        List<ItemResponse> items = itemService.getUserItems(currentUser);
        List<ClaimResponse> claims = claimService.getUserClaims(currentUser);
        List<MessageResponse> messages = messageService.getUserMessages(currentUser);

        String role = currentUser.getAuthorities().iterator().next()
                .getAuthority().replace("ROLE_", "");

        DashboardResponse.UserInfo userInfo = DashboardResponse.UserInfo.builder()
                .name(currentUser.getName())
                .email(currentUser.getEmail())
                .role(role)
                .build();

        DashboardResponse response = DashboardResponse.builder()
                .user(userInfo)
                .items(items)
                .claims(claims)
                .messages(messages)
                .build();

        return ResponseEntity.ok(response);
    }
}