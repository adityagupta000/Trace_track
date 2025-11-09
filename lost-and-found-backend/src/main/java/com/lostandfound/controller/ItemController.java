package com.lostandfound.controller;

import com.lostandfound.dto.request.ItemRequest;
import com.lostandfound.dto.response.ApiResponse;
import com.lostandfound.dto.response.ItemResponse;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.security.UserPrincipal;
import com.lostandfound.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ApiResponse> createItem(
            @Valid @ModelAttribute ItemRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Validate user is authenticated
        if (currentUser == null) {
            throw new BadRequestException("You must be logged in to create an item");
        }

        // Validate image if provided
        if (image != null && !image.isEmpty()) {
            // Check file size (5MB max)
            if (image.getSize() > 5 * 1024 * 1024) {
                throw new BadRequestException("Image size must not exceed 5MB");
            }

            // Check file type
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BadRequestException("Only image files are allowed");
            }
        }

        ItemResponse itemResponse = itemService.createItem(request, image, currentUser);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Item registered successfully")
                .data(itemResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getItems(
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

    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResponse> getItemById(@PathVariable Long itemId) {
        ItemResponse item = itemService.getItemById(itemId);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Item retrieved successfully")
                .data(item)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse> updateItem(
            @PathVariable Long itemId,
            @Valid @ModelAttribute ItemRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Validate user is authenticated
        if (currentUser == null) {
            throw new BadRequestException("You must be logged in to update an item");
        }

        // Validate image if provided
        if (image != null && !image.isEmpty()) {
            if (image.getSize() > 5 * 1024 * 1024) {
                throw new BadRequestException("Image size must not exceed 5MB");
            }

            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BadRequestException("Only image files are allowed");
            }
        }

        ItemResponse itemResponse = itemService.updateItem(itemId, request, image, currentUser);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Item updated successfully")
                .data(itemResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse> deleteItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Validate user is authenticated
        if (currentUser == null) {
            throw new BadRequestException("You must be logged in to delete an item");
        }

        itemService.deleteItem(itemId, currentUser);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Item deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }
}