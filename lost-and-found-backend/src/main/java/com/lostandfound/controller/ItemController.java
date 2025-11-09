package com.lostandfound.controller;

import com.lostandfound.dto.request.ItemRequest;
import com.lostandfound.dto.response.ApiResponse;
import com.lostandfound.dto.response.ItemResponse;
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
        
        ItemResponse itemResponse = itemService.createItem(request, image, currentUser);
        
        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Item registered successfully")
                .data(itemResponse)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<Map<String, List<ItemResponse>>> getItems(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") String status) {
        
        List<ItemResponse> items = itemService.searchItems(search, status);
        
        Map<String, List<ItemResponse>> response = new HashMap<>();
        response.put("items", items);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse> deleteItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        itemService.deleteItem(itemId, currentUser);
        
        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Item deleted successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }
}