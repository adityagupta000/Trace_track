package com.lostandfound.controller;

import com.lostandfound.dto.response.ApiResponse;
import com.lostandfound.dto.response.ClaimResponse;
import com.lostandfound.dto.response.FeedbackResponse;
import com.lostandfound.dto.response.ItemResponse;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.model.User;
import com.lostandfound.repository.UserRepository;
import com.lostandfound.security.UserPrincipal;
import com.lostandfound.service.ClaimService;
import com.lostandfound.service.FeedbackService;
import com.lostandfound.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final ItemService itemService;
    private final ClaimService claimService;
    private final FeedbackService feedbackService;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getAdminDashboard(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (currentUser == null) {
            throw new BadRequestException("You must be logged in");
        }

        // Get all data
        List<ItemResponse> items = itemService.searchItems("", "");
        List<ClaimResponse> claims = claimService.getUserClaims(null); // Get all claims
        List<FeedbackResponse> feedback = feedbackService.getAllFeedback();
        List<User> users = userRepository.findByRoleNot(User.Role.ADMIN);

        List<Map<String, Object>> userList = users.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("name", user.getName());
                    userMap.put("email", user.getEmail());
                    userMap.put("role", user.getRole().name());
                    userMap.put("createdAt", user.getCreatedAt());
                    return userMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("items", items);
        response.put("claims", claims);
        response.put("users", userList);
        response.put("feedback", feedback);
        response.put("stats", Map.of(
                "totalItems", items.size(),
                "totalClaims", claims.size(),
                "totalUsers", userList.size(),
                "totalFeedback", feedback.size()
        ));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse> deleteItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (currentUser == null) {
            throw new BadRequestException("You must be logged in");
        }

        logger.info("Admin {} deleting item {}", currentUser.getEmail(), itemId);

        // Admin can delete any item
        itemService.deleteItemAsAdmin(itemId, currentUser);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Item deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/claims/{claimId}")
    public ResponseEntity<ApiResponse> deleteClaim(
            @PathVariable Long claimId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (currentUser == null) {
            throw new BadRequestException("You must be logged in");
        }

        logger.info("Admin {} deleting claim {}", currentUser.getEmail(), claimId);

        claimService.deleteClaim(claimId);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Claim deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (currentUser == null) {
            throw new BadRequestException("You must be logged in");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getRole() == User.Role.ADMIN) {
            throw new BadRequestException("Cannot delete admin user");
        }

        logger.info("Admin {} deleting user {}", currentUser.getEmail(), user.getEmail());

        userRepository.delete(user);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("User deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/feedback/{feedbackId}")
    public ResponseEntity<ApiResponse> deleteFeedback(
            @PathVariable Long feedbackId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (currentUser == null) {
            throw new BadRequestException("You must be logged in");
        }

        logger.info("Admin {} deleting feedback {}", currentUser.getEmail(), feedbackId);

        feedbackService.deleteFeedback(feedbackId);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Feedback deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/items")
    public ResponseEntity<Map<String, Object>> getAllItems(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") String status) {

        List<ItemResponse> items = itemService.searchItems(search, status);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Items retrieved successfully");
        response.put("items", items);
        response.put("count", items.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/claims")
    public ResponseEntity<Map<String, Object>> getAllClaims() {
        List<ClaimResponse> claims = claimService.getUserClaims(null);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Claims retrieved successfully");
        response.put("claims", claims);
        response.put("count", claims.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        List<User> users = userRepository.findAll();

        List<Map<String, Object>> userList = users.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("name", user.getName());
                    userMap.put("email", user.getEmail());
                    userMap.put("role", user.getRole().name());
                    userMap.put("createdAt", user.getCreatedAt());
                    return userMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Users retrieved successfully");
        response.put("users", userList);
        response.put("count", userList.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/feedback")
    public ResponseEntity<Map<String, Object>> getAllFeedback() {
        List<FeedbackResponse> feedback = feedbackService.getAllFeedback();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Feedback retrieved successfully");
        response.put("feedback", feedback);
        response.put("count", feedback.size());

        return ResponseEntity.ok(response);
    }
}