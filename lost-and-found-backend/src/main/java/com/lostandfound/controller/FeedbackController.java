package com.lostandfound.controller;

import com.lostandfound.dto.request.FeedbackRequest;
import com.lostandfound.dto.response.ApiResponse;
import com.lostandfound.security.UserPrincipal;
import com.lostandfound.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {
    
    private final FeedbackService feedbackService;
    
    @PostMapping
    public ResponseEntity<ApiResponse> submitFeedback(
            @Valid @RequestBody FeedbackRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        ApiResponse response = feedbackService.submitFeedback(request, currentUser);
        return ResponseEntity.ok(response);
    }
}