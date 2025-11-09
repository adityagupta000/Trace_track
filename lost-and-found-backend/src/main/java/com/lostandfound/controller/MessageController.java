package com.lostandfound.controller;

import com.lostandfound.dto.request.MessageRequest;
import com.lostandfound.dto.response.ApiResponse;
import com.lostandfound.dto.response.MessageResponse;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.security.UserPrincipal;
import com.lostandfound.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<ApiResponse> sendMessage(
            @Valid @RequestBody MessageRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Validate user is authenticated
        if (currentUser == null) {
            throw new BadRequestException("You must be logged in to send a message");
        }

        // Validate user is not sending message to themselves
        if (request.getReceiverId().equals(currentUser.getId())) {
            throw new BadRequestException("You cannot send a message to yourself");
        }

        ApiResponse response = messageService.sendMessage(request, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reply")
    public ResponseEntity<ApiResponse> replyMessage(
            @Valid @RequestBody MessageRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Validate user is authenticated
        if (currentUser == null) {
            throw new BadRequestException("You must be logged in to send a reply");
        }

        // Validate user is not replying to themselves
        if (request.getReceiverId().equals(currentUser.getId())) {
            throw new BadRequestException("You cannot send a message to yourself");
        }

        ApiResponse response = messageService.sendMessage(request, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyMessages(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Validate user is authenticated
        if (currentUser == null) {
            throw new BadRequestException("You must be logged in to view messages");
        }

        List<MessageResponse> messages = messageService.getUserMessages(currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Messages retrieved successfully");
        response.put("messages", messages);
        response.put("count", messages.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sent")
    public ResponseEntity<Map<String, Object>> getSentMessages(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Validate user is authenticated
        if (currentUser == null) {
            throw new BadRequestException("You must be logged in to view sent messages");
        }

        List<MessageResponse> messages = messageService.getSentMessages(currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Sent messages retrieved successfully");
        response.put("messages", messages);
        response.put("count", messages.size());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse> deleteMessage(
            @PathVariable Long messageId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Validate user is authenticated
        if (currentUser == null) {
            throw new BadRequestException("You must be logged in to delete a message");
        }

        messageService.deleteMessage(messageId, currentUser);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Message deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }
}