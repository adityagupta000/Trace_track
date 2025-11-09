package com.lostandfound.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FeedbackRequest {
    
    @NotBlank(message = "Feedback text is required")
    private String feedback;
}