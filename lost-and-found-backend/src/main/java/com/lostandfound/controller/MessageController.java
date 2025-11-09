package com.lostandfound.controller;

import com.lostandfound.dto.request.MessageRequest;
import com.lostandfound.dto.response.ApiResponse;
import com.lostandfound.security.UserPrincipal;
import com.lostandfound.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {
    
    private final MessageService messageService;
    
    @PostMapping
    public ResponseEntity<ApiResponse> sendMessage(
            @Valid @RequestBody MessageRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        ApiResponse response = messageService.sendMessage(request, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reply")
    public ResponseEntity<ApiResponse> replyMessage(
            @Valid @RequestBody MessageRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        ApiResponse response = messageService.sendMessage(request, currentUser);
        return ResponseEntity.ok(response);
    }
}