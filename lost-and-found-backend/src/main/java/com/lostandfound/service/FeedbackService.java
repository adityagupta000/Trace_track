package com.lostandfound.service;

import com.lostandfound.dto.request.FeedbackRequest;
import com.lostandfound.dto.response.ApiResponse;
import com.lostandfound.dto.response.FeedbackResponse;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.model.Feedback;
import com.lostandfound.model.User;
import com.lostandfound.repository.FeedbackRepository;
import com.lostandfound.repository.UserRepository;
import com.lostandfound.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public ApiResponse submitFeedback(FeedbackRequest request, UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
        
        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setFeedbackText(request.getFeedback());
        
        feedbackRepository.save(feedback);
        
        return ApiResponse.builder()
                .success(true)
                .message("Thank you for your feedback!")
                .build();
    }
    
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getAllFeedback() {
        List<Feedback> feedbacks = feedbackRepository.findAllByOrderBySubmittedAtDesc();
        return feedbacks.stream()
                .map(this::mapToFeedbackResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteFeedback(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", feedbackId));
        
        feedbackRepository.delete(feedback);
    }
    
    private FeedbackResponse mapToFeedbackResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .userId(feedback.getUser().getId())
                .userName(feedback.getUser().getName())
                .feedbackText(feedback.getFeedbackText())
                .submittedAt(feedback.getSubmittedAt())
                .build();
    }
}