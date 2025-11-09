package com.lostandfound.controller;

import com.lostandfound.dto.request.FeedbackRequest;
import com.lostandfound.dto.response.ApiResponse;
import com.lostandfound.exception.BadRequestException;
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

        // Validate user is authenticated
        if (currentUser == null) {
            throw new BadRequestException("You must be logged in to submit feedback");
        }

        // Additional validation for feedback content
        if (request.getFeedback() == null || request.getFeedback().trim().isEmpty()) {
            throw new BadRequestException("Feedback content cannot be empty");
        }

        if (request.getFeedback().trim().length() < 10) {
            throw new BadRequestException("Feedback must be at least 10 characters long");
        }

        if (request.getFeedback().trim().length() > 2000) {
            throw new BadRequestException("Feedback cannot exceed 2000 characters");
        }

        ApiResponse response = feedbackService.submitFeedback(request, currentUser);
        return ResponseEntity.ok(response);
    }
}